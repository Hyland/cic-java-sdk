# CIC Knowledge Enrichment Module (`cic-ke`)

Java client for the Hyland Content Intelligence Cloud **Knowledge Enrichment** APIs.

This module provides two independent API clients:

- **Context API** -- Infers metadata from unstructured content (classification, summarization, NER, embeddings, metadata generation).
- **Data Curation API** -- Transforms raw content into structured, AI-ready data (text extraction, PII redaction, chunking, embedding generation), with a Configuration API for managing processing defaults and conditional rules.

---

## Table of Contents

- [Maven Dependency](#maven-dependency)
- [Authentication Setup](#authentication-setup)
- [Creating a CICBlob](#creating-a-cicblob)
- [Context API](#context-api)
  - [Enrich a Document (End-to-End)](#enrich-a-document-end-to-end)
  - [Enrich with Custom Options](#enrich-with-custom-options)
  - [Step-by-Step Workflow](#step-by-step-workflow)
  - [Send for Enrichment + Poll Separately](#send-for-enrichment--poll-separately)
  - [Get Presigned URL](#get-presigned-url)
  - [Upload a File](#upload-a-file)
  - [Submit a Process Request](#submit-a-process-request)
  - [Get Results Directly](#get-results-directly)
  - [Poll for Results](#poll-for-results)
  - [List Available Actions](#list-available-actions)
  - [Health Check](#health-check)
  - [Reading Enrichment Results](#reading-enrichment-results)
- [Data Curation API](#data-curation-api)
  - [Curate a Document (End-to-End)](#curate-a-document-end-to-end)
  - [Curate with Builder Consumer](#curate-with-builder-consumer)
  - [Step-by-Step Curation Workflow](#step-by-step-curation-workflow)
  - [Check Job Status](#check-job-status)
  - [List Embedding Models](#list-embedding-models)
  - [Health Check (Data Curation)](#health-check-data-curation)
- [Configuration API](#configuration-api)
  - [Initialize Configuration](#initialize-configuration)
  - [Get Full Configuration](#get-full-configuration)
  - [Get Configuration Defaults](#get-configuration-defaults)
  - [Update Configuration Defaults](#update-configuration-defaults)
  - [Reset Configuration Defaults](#reset-configuration-defaults)
  - [List Configuration Rules](#list-configuration-rules)
  - [Create a Configuration Rule](#create-a-configuration-rule)
  - [Get a Configuration Rule](#get-a-configuration-rule)
  - [Update a Configuration Rule](#update-a-configuration-rule)
  - [Delete a Configuration Rule](#delete-a-configuration-rule)
  - [Test Configuration Rules (Dry Run)](#test-configuration-rules-dry-run)
- [Processing Options Reference](#processing-options-reference)
- [Supported Context API Actions](#supported-context-api-actions)
- [Polling Configuration](#polling-configuration)

---



## Maven Dependency

```xml
<dependency>
    <groupId>org.hyland.sdk</groupId>
    <artifactId>cic-ke</artifactId>
    <version>${cic-java-sdk.version}</version>
</dependency>
```

A JSON mapper is required at runtime. Add the Jackson2 implementation:

```xml
<dependency>
    <groupId>org.hyland.sdk</groupId>
    <artifactId>cic-http-client-jackson2</artifactId>
    <version>${cic-java-sdk.version}</version>
</dependency>
```

---



## Authentication Setup

Both clients use **OAuth2 client credentials** to authenticate. You will need four values from your Hyland environment:


| Parameter              | Description                                                                                                                        |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **API base URL**       | The root URL of the KE service you want to call. Provided by Hyland for your region and environment (dev, staging, production).    |
| **Token endpoint URL** | The OAuth2 identity provider URL where the SDK exchanges your client credentials for an access token. Also provided by Hyland.     |
| **Client ID / Secret** | Your application's OAuth2 credentials, obtained when you register your app with the Hyland identity platform.                      |
| **HXP Environment ID** | Identifies which Content Intelligence environment (tenant) your requests target. Found in your Hyland Experience Platform console. |


> **Example URLs below are illustrative only.** Replace them with the actual URLs for your environment.

```java
// Context API client
var keClient = KEHttpClient.from(
        "https://ke.api.example.hyland.com",                        // your Context API base URL
        AuthenticationHttpClient.from("https://auth.example.hyland.com")) // your OAuth2 token endpoint
    .clientId("your-client-id")
    .clientSecret("your-client-secret")
    .hxpEnvironment("your-environment-id")
    .build();

var keService = new KEService(keClient);
```

```java
// Data Curation API client (uses a different base URL but the same token endpoint)
var dcClient = DataCurationHttpClient.from(
        "https://dc.api.example.hyland.com",                        // your Data Curation API base URL
        AuthenticationHttpClient.from("https://auth.example.hyland.com")) // same token endpoint
    .clientId("your-client-id")
    .clientSecret("your-client-secret")
    .hxpEnvironment("your-environment-id")
    .build();

var dcService = new DataCurationService(dcClient);
```

If you omit the `AuthenticationHttpClient.from(...)` parameter, the SDK defaults to the Hyland dev token endpoint:

```java
// Shorthand — uses the built-in dev token endpoint
var keClient = KEHttpClient.from("https://ke.api.example.hyland.com")
                           .clientId("your-client-id")
                           .clientSecret("your-client-secret")
                           .hxpEnvironment("your-environment-id")
                           .build();
```

---



## Creating a CICBlob

`CICBlob` is a simple interface that the SDK uses to read file content for upload. It has three methods:


| Method             | Purpose                                                                                                            |
| ------------------ | ------------------------------------------------------------------------------------------------------------------ |
| `getInputStream()` | Returns an `InputStream` of the file bytes. Called once; the SDK buffers the content to a temporary file for retries. |
| `getDigest()`      | Optional content hash. Return `Optional.empty()` if not available.                                                 |
| `getContentType()` | MIME type of the file (e.g. `"application/pdf"`, `"image/jpeg"`). Used as the `Content-Type` header during upload. |


**Example — from a file on disk:**

```java
Path filePath = Path.of("path/to/your/document.pdf");

CICBlob blob = new CICBlob() {
    @Override
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> getDigest() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.of("application/pdf");
    }
};
```

**Example — from a byte array (e.g. content from a database or HTTP response):**

```java
byte[] imageBytes = ...; // your image bytes
CICBlob imageBlob = new CICBlob() {
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(imageBytes);
    }

    @Override
    public Optional<String> getDigest() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.of("image/jpeg");
    }
};
```

---



## Context API

Use `KEService` for all Context API operations. It wraps `KEHttpClient` with convenience methods, polling, and null-safety.

Key types used in the examples below:


| Type                   | Description                                                                                                                                                                                                                  |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Action`               | Enum of available enrichment actions (e.g. `TEXT_SUMMARIZATION`, `PRETRAINED_CLASSIFICATION`). Call `.value()` to get the camelCase API string. See [Supported Context API Actions](#supported-context-api-actions) for the full list. |
| `ObjectKey`            | Identifies content by either an uploaded object `path` or an existing platform `documentId`. Exactly one identifier is required.                                                                                             |
| `ProcessRequest`       | A v2 process request. Specifies object keys and a map of actions, where each action can carry its own configuration (`ActionConfig`). Built using `ProcessRequest.builder()`.                                                 |
| `ActionConfig`         | Per-action configuration: `classes`, `maxWordCount`, `kSimilarMetadata`, `instructions`, `category`, and `model`. Use `ActionConfig.builder()` or the `action(Action, cfg -> ...)` shorthand on the request builder. |
| `EnrichmentResult`     | The response from the server containing status, timestamp, and a list of per-object results.                                                                                                                                 |
| `ActionDescriptor`     | Describes an available action, including its `name`, `availableModels`, and `availableCategories`. Retrieved via `getActionDescriptors()`.                                                                                    |
| `ClassificationResult` | Result of a pretrained classification action, containing `classification` (label) and `confidence` (score).                                                                                                                   |
| `ProcessingError`      | A document-level processing error containing an `errorType` and human-readable `message`.                                                                                                                                    |




### Enrich a Document (End-to-End)

The simplest way to enrich a document. Handles upload, processing, and polling automatically.

```java
CICBlob blob = ...; // see "Creating a CICBlob" above

EnrichmentResult result = keService.enrich(blob, List.of(
    Action.TEXT_SUMMARIZATION,
    Action.NAMED_ENTITY_RECOGNITION_TEXT
));

System.out.println("Status: " + result.status());       // "SUCCESS"
System.out.println("Complete: " + result.isComplete());  // true
```



### Enrich with Custom Options

Use a builder consumer to set per-action configuration. In v2 format, each action carries its own `classes`, `maxWordCount`, `kSimilarMetadata`, and `instructions`.

```java
EnrichmentResult result = keService.enrich(blob, req -> req
    .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(200))
    .action(Action.TEXT_CLASSIFICATION, cfg -> cfg
        .classes(List.of("Legal", "Financial", "Technical"))
        .instruction("context", "corporate documents"))
);
```



### Pretrained Classification

Use the `PRETRAINED_CLASSIFICATION` action with a `category` and `model` to classify content using a pretrained model. Use `getActionDescriptors()` to discover available models and categories for your account.

```java
EnrichmentResult result = keService.enrich(blob, req -> req
    .action(Action.PRETRAINED_CLASSIFICATION, cfg -> cfg
        .category("MediaType")
        .model("pretrained-model-a"))
);

var entry = result.results().get(0);
if (entry.pretrainedClassification() != null && entry.pretrainedClassification().isSuccess()) {
    var classification = entry.pretrainedClassification().result();
    System.out.println("Class: " + classification.classification()); // e.g. "CT"
    System.out.println("Confidence: " + classification.confidence()); // e.g. 0.95
}
```



### Step-by-Step Workflow

For full control over each step of the enrichment pipeline.

```java
// 1. Get a presigned upload URL
PresignedUrl presignedUrl = keService.getPresignedUrl("application/pdf");

// 2. Upload the file
keService.upload(presignedUrl.presignedUrl(), blob);

// 3. Submit for processing (v2 format: actions are an object map)
String processingId = keService.process(req -> req
    .objectPath(presignedUrl.objectKey())
    .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(300))
    .action(Action.IMAGE_DESCRIPTION)
);

// 4. Poll for results (blocks until ready or timeout)
EnrichmentResult result = keService.pollResults(processingId);
```



### Send for Enrichment + Poll Separately

Upload and submit in one call, then poll separately. Useful when you want to do other work between submission and polling.

```java
// The uploaded file's object key is added automatically.
String processingId = keService.sendForEnrichment(blob, request -> request
    .action(Action.IMAGE_DESCRIPTION)
    .action(Action.IMAGE_METADATA_GENERATION, cfg -> cfg
        .addSimilarMetadata(Map.of("title", "Annual Report"))
        .instruction("detailLevel", "high")));

// ... do other work ...

// Poll when ready
EnrichmentResult result = keService.pollResults(processingId);
```



### Get Presigned URL

Obtain a presigned URL to upload a file of a given content type.

```java
PresignedUrl presigned = keService.getPresignedUrl("image/jpeg");

System.out.println(presigned.presignedUrl()); // a temporary signed URL for uploading
System.out.println(presigned.objectKey());    // e.g. "contents/abc123-photo.jpg"
```



### Upload a File

Upload a file to a presigned URL.

```java
keService.upload(presigned.presignedUrl(), blob);
```



### Submit a Process Request

Submit a process request with specific object keys and actions. In v2, each action's configuration (classes, instructions, maxWordCount) is nested inside the action itself.

```java
// Using a builder — multiple actions with per-action config
ProcessRequest request = ProcessRequest.builder()
    .objectPath("contents/my-document.pdf")
    .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(500))
    .action(Action.NAMED_ENTITY_RECOGNITION_TEXT)
    .build();

String processingId = keService.process(request);
```

Use a document ID when processing content that is already known to the platform:

```java
String processingId = keService.process(req -> req
    .documentId("document-123")
    .action(Action.TEXT_SUMMARIZATION)
);
```

```java
// Using a consumer — classification with instructions
String processingId = keService.process(req -> req
    .objectPath("contents/my-document.pdf")
    .action(Action.TEXT_CLASSIFICATION, cfg -> cfg
        .classes(List.of("invoice", "contract", "other"))
        .instruction("context", "financial documents"))
    .action(Action.TEXT_EMBEDDINGS)
);
```



### Get Results Directly

Fetch results immediately (throws if not ready yet).

```java
EnrichmentResult result = keService.getResults(processingId);

System.out.println("ID: " + result.id());           // e.g. "proc-abc-123"
System.out.println("Status: " + result.status());    // e.g. "SUCCESS"
System.out.println("Results: " + result.results().size()); // e.g. 1
```



### Poll for Results

Poll with automatic retries until results are ready or timeout.

```java
EnrichmentResult result = keService.pollResults(processingId);
// Blocks until the server returns a 200 (ready) instead of 202 (still processing).
// Throws CICSdkException if max attempts are exceeded.
```



### List Available Actions

Get structured descriptors for the actions supported by the server, including available models and categories:

```java
List<ActionDescriptor> descriptors = keService.getActionDescriptors();

for (var descriptor : descriptors) {
    System.out.println("Action: " + descriptor.name());
    if (!descriptor.availableModels().isEmpty()) {
        System.out.println("  Models: " + descriptor.availableModels());
    }
    if (!descriptor.availableCategories().isEmpty()) {
        System.out.println("  Categories: " + descriptor.availableCategories());
    }
}
// e.g. "Action: pretrainedClassification"
//      "  Models: [pretrained-model-a, pretrained-model-b]"
//      "  Categories: [MediaType, OrganSystem]"
```



### Health Check

Check if the Context API service is available.

```java
boolean healthy = keService.isHealthy();
System.out.println("Context API healthy: " + healthy); // true
```



### Reading Enrichment Results

Each result entry contains an `ActionResult<T>` for every requested action. Each `ActionResult` has `isSuccess()`, `result()`, and `error()`.

```java
for (var entry : result.results()) {
    System.out.println("Object: " + entry.objectKey());

    // Text Summary (String result)
    if (entry.textSummary() != null && entry.textSummary().isSuccess()) {
        System.out.println("Summary: " + entry.textSummary().result());
    }

    // Named Entities (Map<String, List<String>> result)
    if (entry.namedEntityText() != null && entry.namedEntityText().isSuccess()) {
        Map<String, List<String>> entities = entry.namedEntityText().result();
        entities.forEach((type, values) ->
            System.out.println(type + ": " + values));
        // e.g. PERSON: [John Doe, Jane Smith]
        //      ORG: [Hyland Software]
    }

    // Text Classification (String result)
    if (entry.textClassification() != null && entry.textClassification().isSuccess()) {
        System.out.println("Class: " + entry.textClassification().result());
    }

    // Image Description (String result)
    if (entry.imageDescription() != null && entry.imageDescription().isSuccess()) {
        System.out.println("Description: " + entry.imageDescription().result());
    }

    // Text Embeddings (one vector per text chunk)
    if (entry.textEmbeddings() != null && entry.textEmbeddings().isSuccess()) {
        List<List<Double>> vectors = entry.textEmbeddings().result();
        for (int chunk = 0; chunk < vectors.size(); chunk++) {
            System.out.println("Chunk " + chunk + " embedding dimension: " + vectors.get(chunk).size());
        }
    }

    // Image Embeddings (one vector for the image)
    if (entry.imageEmbeddings() != null && entry.imageEmbeddings().isSuccess()) {
        List<Double> vector = entry.imageEmbeddings().result();
        System.out.println("Image embedding dimension: " + vector.size());
    }

    // Text Metadata (Map<String, Object> result)
    if (entry.textMetadata() != null && entry.textMetadata().isSuccess()) {
        Map<String, Object> metadata = entry.textMetadata().result();
        metadata.forEach((key, value) ->
            System.out.println(key + " = " + value));
    }

    // Pretrained Classification (ClassificationResult with classification + confidence)
    if (entry.pretrainedClassification() != null && entry.pretrainedClassification().isSuccess()) {
        var classification = entry.pretrainedClassification().result();
        System.out.println("Classification: " + classification.classification());
        System.out.println("Confidence: " + classification.confidence());
    }

    // Error handling
    if (entry.textSummary() != null && !entry.textSummary().isSuccess()) {
        System.err.println("Summary failed: " + entry.textSummary().error().message());
    }

    // General processing errors
    for (var error : entry.generalProcessingErrors()) {
        System.err.println(error.errorType() + ": " + error.message());
    }
}
```

---



## Data Curation API

Use `DataCurationService` for all Data Curation operations.

Key types used in the examples below:


| Type                | Description                                                                                                                                                                                                             |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ProcessingOptions` | Controls how the curation pipeline processes content — chunking, embedding, PII redaction, normalization. Built using `ProcessingOptions.builder()`. See [Processing Options Reference](#processing-options-reference). |
| `PresignResponse`   | Contains a `jobId`, a `putUrl` (for uploading), and a `getUrl` (for downloading results).                                                                                                                               |
| `JobStatus`         | The job's current state. Lifecycle: PENDING -> PROCESSING -> COMPLETED/FAILED. Use `isDone()` for success, `isFailed()` for terminal failure, `isTerminal()` for any final state, `hasError()` to detect error payloads. |




### Curate a Document (End-to-End)

The simplest way to curate a document. Handles presigning, upload, status polling, and result download automatically.

```java
CICBlob blob = ...; // see "Creating a CICBlob" above

String resultJson = dcService.curate(blob, ProcessingOptions.builder()
    .chunking(true)
    .chunkSize(2000)
    .embedding(true)
    .embeddingsModel("cohere.embed-multilingual-v3")
    .build()
);

System.out.println(resultJson); // raw JSON with chunks, embeddings, etc.
```



### Curate with Builder Consumer

```java
String resultJson = dcService.curate(blob, opts -> opts
    .chunking(true)
    .chunkSize(1500)
    .chunkingStrategy("context")
    .embedding(true)
    .pii(pii -> pii.mode("redaction").entityRedaction(true))
    .normalization(norm -> norm.quotations(true).dashes(true))
);
```



### Curate with Default Options

Pass `null` to use the server's default processing options (or the environment defaults set via the Configuration API).

```java
String resultJson = dcService.curate(blob, (ProcessingOptions) null);
```



### Step-by-Step Curation Workflow

For full control over each step of the curation pipeline.

```java
// 1. Get presigned upload and download URLs
PresignResponse presign = dcService.presign(opts -> opts
    .chunking(true)
    .chunkSize(2000)
    .embedding(true)
);

System.out.println("Job ID: " + presign.jobId());       // e.g. "job-abc-123"
System.out.println("Upload URL: " + presign.putUrl());   // a temporary signed URL for uploading
System.out.println("Download URL: " + presign.getUrl()); // a temporary signed URL for downloading results

// 2. Upload the file to the presigned PUT URL
dcClient.upload(presign.putUrl(), blob);

// 3. Poll until terminal state
JobStatus status;
do {
    Thread.sleep(5000);
    status = dcService.getJobStatus(presign.jobId());
    System.out.println("Status: " + status.status());
} while (!status.isTerminal());

// 4. Check for failures before downloading
if (status.isFailed()) {
    throw new RuntimeException("Job failed: " + status.errorMessage());
}
if (status.hasError()) {
    // COMPLETED but with an error payload (e.g. non-retryable delivery failure)
    throw new RuntimeException("Job completed with error: " + status.errorMessage());
}

// 5. Download the result
String resultJson = dcClient.downloadResult(presign.getUrl());
```



### Check Job Status

```java
JobStatus status = dcService.getJobStatus("job-abc-123");

System.out.println(status.jobId());       // "job-abc-123"
System.out.println(status.status());      // "PENDING", "PROCESSING", "COMPLETED", or "FAILED"
System.out.println(status.isDone());      // true only when COMPLETED with no error
System.out.println(status.isFailed());    // true when pipeline exhausted retries
System.out.println(status.isTerminal());  // true when COMPLETED or FAILED
System.out.println(status.hasError());    // true when response contains error payload
System.out.println(status.errorMessage());// error detail (may be null)
```



### List Embedding Models

List the embedding models available on the server.

```java
List<EmbeddingModel> models = dcService.listModels();

for (var model : models) {
    System.out.println(model.name() + " (max chunk: " + model.maxChunkSize() + ")");
    System.out.println("  Precisions: " + model.supportedPrecisions());
    System.out.println("  Dimensions: " + model.supportedOutputDimensions());
}
// e.g. "cohere.embed-multilingual-v3 (max chunk: 512)"
//      "  Precisions: [float32, int8]"
//      "  Dimensions: [1024]"
```



### Health Check (Data Curation)

```java
boolean healthy = dcClient.isHealthy();
System.out.println("Data Curation API healthy: " + healthy); // true
```

---



## Configuration API

The Configuration API is part of the Data Curation API. It manages environment-level processing defaults and conditional rules that control how the curation pipeline behaves automatically. All configuration methods are accessed through `DataCurationService` (the same `dcService` created above).

Key types used in the examples below:


| Type            | Description                                                                                                                                               |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ConfigOptions` | The full configuration snapshot: a `defaults` block (`ProcessingOptions`) plus a list of `rules` (`ConfigRule`).                                          |
| `ConfigRule`    | A conditional rule with a `name`, a list of `conditions` (field/value pairs), and a `config` block that overrides the defaults when the conditions match. |




### Initialize Configuration

Creates the configuration with system defaults. Call this once per environment.

```java
ConfigOptions config = dcService.initializeConfig();

System.out.println("Defaults: " + config.defaults().chunking()); // e.g. false
System.out.println("Rules: " + config.rules().size());           // 0 (initially empty)
```



### Get Full Configuration

Retrieves the complete configuration including defaults and all rules.

```java
ConfigOptions config = dcService.getConfig();

System.out.println("Chunking: " + config.defaults().chunking());     // e.g. true
System.out.println("Chunk size: " + config.defaults().chunkSize());   // e.g. 1500
System.out.println("Embedding: " + config.defaults().embedding());    // e.g. true
System.out.println("Number of rules: " + config.rules().size());      // e.g. 2
```



### Get Configuration Defaults

Retrieves only the defaults section.

```java
ProcessingOptions defaults = dcService.getConfigDefaults();

System.out.println("Chunking: " + defaults.chunking());              // e.g. true
System.out.println("Chunk size: " + defaults.chunkSize());            // e.g. 1500
System.out.println("Embedding: " + defaults.embedding());             // e.g. true
System.out.println("Embeddings model: " + defaults.embeddingsModel()); // e.g. "cohere.embed-multilingual-v3"
```



### Update Configuration Defaults

Replace the environment defaults with new values.

```java
ProcessingOptions updated = dcService.updateConfigDefaults(
    ProcessingOptions.builder()
        .chunking(true)
        .chunkSize(1500)
        .chunkingStrategy("context")
        .embedding(true)
        .embeddingsModel("cohere.embed-multilingual-v3")
        .pii(pii -> pii.mode("redaction"))
        .normalization(norm -> norm.quotations(true).dashes(true))
        .build()
);

System.out.println("Updated chunk size: " + updated.chunkSize());
```



### Reset Configuration Defaults

Reset defaults back to the system defaults.

```java
dcService.resetConfigDefaults();
```



### List Configuration Rules

```java
List<ConfigRule> rules = dcService.listConfigRules();

for (var rule : rules) {
    System.out.println(rule.id() + " - " + rule.name());
    for (var condition : rule.conditions()) {
        System.out.println("  when " + condition.field() + " = " + condition.value());
    }
    System.out.println("  chunk size: " + rule.config().chunkSize());
}
// e.g. "rule-1 - Large PDF Rule"
//      "  when content_type = application/pdf"
//      "  chunk size: 3000"
```



### Create a Configuration Rule

Rules override the defaults when their conditions match a document's properties.

```java
// Using a builder consumer
ConfigRule created = dcService.createConfigRule(rule -> rule
    .name("Large PDF Rule")
    .addCondition("content_type", "application/pdf")
    .config(ProcessingOptions.builder()
        .chunkSize(3000)
        .chunking(true)
        .embedding(true)
        .build()
    )
);

System.out.println("Created rule: " + created.id()); // e.g. "rule-xyz-789"
```

```java
// Using a ConfigRule object
ConfigRule rule = ConfigRule.builder()
    .name("Image Processing Rule")
    .addCondition("content_type", "image/jpeg")
    .config(ProcessingOptions.builder()
        .embedding(true)
        .embeddingsModel("cohere.embed-multilingual-v3")
        .build()
    )
    .build();

ConfigRule created = dcService.createConfigRule(rule);
```



### Get a Configuration Rule

```java
ConfigRule rule = dcService.getConfigRule("rule-abc-123");

System.out.println(rule.name());                // e.g. "Large PDF Rule"
System.out.println(rule.config().chunkSize());   // e.g. 3000
System.out.println(rule.config().embedding());   // e.g. true
```



### Update a Configuration Rule

```java
ConfigRule updated = dcService.updateConfigRule("rule-abc-123",
    ConfigRule.builder()
        .name("Updated PDF Rule")
        .addCondition("content_type", "application/pdf")
        .config(ProcessingOptions.builder()
            .chunkSize(4000)
            .embedding(true)
            .build()
        )
        .build()
);
```



### Delete a Configuration Rule

```java
dcService.deleteConfigRule("rule-abc-123");
```



### Test Configuration Rules (Dry Run)

Preview which rule would match a document's properties without actually processing anything.

```java
RuleTestResponse testResult = dcService.testConfigRules(
    RuleTestRequest.builder()
        .property("content_type", "application/pdf")
        .property("file_size", "5000000")
        .build()
);

// The matched rule (or null if no rule matched)
if (testResult.matchedRule() != null) {
    System.out.println("Matched rule: " + testResult.matchedRule().name());
}

// The effective config (defaults merged with matched rule overrides)
ProcessingOptions effective = testResult.effectiveConfig();
System.out.println("Effective chunk size: " + effective.chunkSize());
System.out.println("Effective embedding: " + effective.embedding());
```

---



## Processing Options Reference

All fields in `ProcessingOptions` are optional. Only set what you need.

```java
ProcessingOptions options = ProcessingOptions.builder()
    .chunking(true)                                    // enable chunking
    .chunkSize(2000)                                   // tokens per chunk
    .chunkingStrategy("context")                       // chunking strategy
    .embedding(true)                                   // generate embeddings
    .embeddingsModel("cohere.embed-multilingual-v3")   // model to use
    .embeddingPrecision("float32")                     // embedding precision
    .jsonSchema("{ ... }")                             // structured extraction schema
    .normalization(norm -> norm                        // text normalization
        .quotations(true)                              //   normalize quotation marks
        .dashes(true)                                  //   normalize dashes
    )
    .pii(pii -> pii                                    // PII handling
        .mode("redaction")                             //   redaction mode
        .entityRedaction(true)                         //   redact entities
    )
    .build();
```

---



## Supported Context API Actions

All actions use **v2 format** (camelCase naming). Classification and metadata generation actions accept per-action `instructions`. Embedding actions do not accept instructions.


| Action                     | Enum Constant                    | v2 API Value (camelCase)         | Input                           | Instructions |
| -------------------------- | -------------------------------- | -------------------------------- | ------------------------------- | ------------ |
| Text Summary               | `TEXT_SUMMARIZATION`             | `textSummarization`              | Document                        | No           |
| Classify Text              | `TEXT_CLASSIFICATION`            | `textClassification`             | Document + classes              | Yes          |
| Text NER                   | `NAMED_ENTITY_RECOGNITION_TEXT`  | `namedEntityRecognitionText`     | Document                        | No           |
| Text Embeddings            | `TEXT_EMBEDDINGS`                | `textEmbeddings`                 | Document                        | No           |
| Text Metadata              | `TEXT_METADATA_GENERATION`       | `textMetadataGeneration`         | Document + kSimilarMetadata     | Yes          |
| Image Description          | `IMAGE_DESCRIPTION`              | `imageDescription`               | Image                           | No           |
| Classify Image             | `IMAGE_CLASSIFICATION`           | `imageClassification`            | Image + classes                 | Yes          |
| Image NER                  | `NAMED_ENTITY_RECOGNITION_IMAGE` | `namedEntityRecognitionImage`    | Image                           | No           |
| Image Embeddings           | `IMAGE_EMBEDDINGS`               | `imageEmbeddings`                | Image                           | No           |
| Image Metadata             | `IMAGE_METADATA_GENERATION`      | `imageMetadataGeneration`        | Image + kSimilarMetadata        | Yes          |
| Pretrained Classification  | `PRETRAINED_CLASSIFICATION`      | `pretrainedClassification`       | Document/Image + category/model | No           |


Pretrained classification is available only when the caller has access to a supported category and model. Use `getActionDescriptors()` to discover whether it is enabled for the current environment.


Text-based actions support all document formats that Data Curation supports (hundreds of formats), not just PDF.

---



## Polling Configuration

Both services poll asynchronously for results. Configure the polling behavior:

```java
// KEService -- default: 20 attempts, 5s intervals
keService.setPollSettings(30, Duration.ofSeconds(10));  // 30 attempts, 10 second intervals

// DataCurationService -- default: 20 attempts, 5s intervals
dcService.setPollSettings(15, Duration.ofSeconds(3));   // 15 attempts, 3 second intervals
```

If polling exceeds the max attempts, a `CICSdkException` is thrown.

---



## Error Handling

API errors are thrown as `CICServiceException`, which wraps a `CICError` containing structured error details. The SDK supports both legacy error formats and RFC 9457 Problem Details responses.

```java
try {
    keService.enrich(blob, req -> req
        .action(Action.PRETRAINED_CLASSIFICATION, cfg -> cfg
            .category("MediaType")
            .model("invalid-model"))
    );
} catch (CICServiceException e) {
    System.err.println("Status: " + e.statusCode());          // e.g. 400
    System.err.println("Message: " + e.getMessage());         // human-readable summary

    e.remoteCause().ifPresent(error -> {
    if (error.isProblemDetail()) {
        // RFC 9457 Problem Details fields
        System.err.println("Type: " + error.type());          // URI reference
        System.err.println("Title: " + error.title());        // short summary
        System.err.println("Detail: " + error.detail());      // explanation
        System.err.println("Instance: " + error.instance());  // occurrence URI

        // API-specific extension fields (e.g. validation errors)
        Map<String, Object> extensions = error.extensions();
        if (extensions.containsKey("errors")) {
            System.err.println("Validation errors: " + extensions.get("errors"));
        }
    }
    });
}
```

---



## CI / E2E Testing

The project includes end-to-end tests that run against a real staging CIC environment. These are excluded from the default `mvn install` and only run when the `e2e` Maven profile is activated.

### Running E2E Tests Locally

```bash
export CIC_KE_CLIENT_ID="your-client-id"
export CIC_KE_CLIENT_SECRET="your-client-secret"
export CIC_KE_BASE_URL="https://api.app.hyland.com"
export CIC_KE_AUTH_URL="https://auth.iam.hyland.com"
export CIC_KE_DC_BASE_URL="https://dc.app.hyland.com"

mvn verify -Pe2e -pl cic-ke -am
```

If the environment variables are not set, E2E tests are skipped (not failed) via JUnit 5 assumptions.

### GitHub Actions

The `integration-test` job in `.github/workflows/build.yml` runs E2E tests automatically on pushes to `main` and on manual `workflow_dispatch` triggers. It requires the following **repository secrets** (Settings > Secrets and variables > Actions):

| Secret | Description |
|--------|-------------|
| `CIC_KE_CLIENT_ID` | OAuth2 client ID for the staging CIC application |
| `CIC_KE_CLIENT_SECRET` | OAuth2 client secret for the staging CIC application |
| `CIC_KE_BASE_URL` | Context API base URL (e.g. `https://api.app.hyland.com`) |
| `CIC_KE_AUTH_URL` | Auth token endpoint base URL (e.g. `https://auth.iam.hyland.com`) |
| `CIC_KE_DC_BASE_URL` | Data Curation API base URL (e.g. `https://dc.app.hyland.com`) |