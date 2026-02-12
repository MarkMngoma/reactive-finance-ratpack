# OpenAPI Specification Troubleshooting Guide

## Issue: Tags and Descriptions Not Appearing

If you're seeing an OpenAPI spec without tags and descriptions, the spec files are likely correct but you're viewing a cached version.

### Current Spec Status ✅

Both spec files contain:
- ✅ All 7 tags defined with descriptions
- ✅ All operations have `summary` field
- ✅ All operations have `description` field  
- ✅ All operations have `tags` array
- ✅ 3 schemas in components section

**Example from current spec:**
```yaml
/v1/WriteModificationCurrencyResource:
  put:
    summary: Modify existing currency
    description: Modify an existing currency resource with updated JSON payload
    operationId: givenExistingCurrencyWhenModifyingThenVerifyNoContentResponse
    tags:
    - Write
    - Currency
    - Modification
```

## Solutions

### 1. Hard Refresh Browser (Fastest)

**Windows/Linux:**
- Press `Ctrl + Shift + R` or `Ctrl + F5`

**Mac:**
- Press `Cmd + Shift + R`

### 2. Clear Browser Cache

1. Open Developer Tools (`F12`)
2. Right-click the refresh button
3. Select "Empty Cache and Hard Reload"

### 3. Restart Ratpack Application

```bash
# Stop the application (Ctrl+C)
# Then clean and restart
./gradlew clean
./gradlew run
```

### 4. Verify Spec Directly

Check the spec file is being served correctly:

```bash
# View spec directly
curl http://localhost:5051/api-docs/openapi.yaml | head -100

# Check tags section
curl http://localhost:5051/api-docs/openapi.yaml | grep -A 3 "tags:"

# Check specific operation
curl http://localhost:5051/api-docs/openapi.yaml | grep -A 10 "WriteModificationCurrencyResource"
```

### 5. Check File Locally

Verify the spec files on disk:

```bash
# Check main spec
cat src/main/resources/api-docs/openapi.yaml | head -100

# Check test spec
cat src/test/resources/spec/openapi.yaml | head -100

# Verify they match
diff src/main/resources/api-docs/openapi.yaml src/test/resources/spec/openapi.yaml
```

## Verification Commands

### Check Tags
```bash
grep -n "tags:" src/main/resources/api-docs/openapi.yaml | head -20
```

Expected output: Lines showing tag definitions and operation tags

### Check Descriptions
```bash
grep -n "description:" src/main/resources/api-docs/openapi.yaml | head -20
```

Expected output: Multiple description fields for tags, operations, and schemas

### Check Summaries
```bash
grep -n "summary:" src/main/resources/api-docs/openapi.yaml | head -20
```

Expected output: Summary fields for all operations

## Access Points

### Ratpack Integration
- **UI:** http://localhost:5051/api-docs
- **Spec:** http://localhost:5051/api-docs/openapi.yaml

### Docker Compose (Alternative)
- **UI:** http://localhost:8080
- **Spec:** http://localhost:8080/openapi.yaml

## Expected Scalar UI Display

When working correctly, you should see:

```
📂 Currency
  ├─ POST Create batch currencies
  ├─ POST Create single currency
  └─ PUT Modify existing currency

📂 Write
  ├─ POST Create single currency
  └─ PUT Modify existing currency

📂 Query
  └─ GET Query currency by code

📂 QueryBatch
  └─ GET Query all currencies

... etc
```

Each operation should show:
- Summary (title)
- Description (detailed explanation)
- Tags (grouping categories)
- Parameters
- Request/Response examples

## Still Having Issues?

1. **Check Scalar UI Console**
   - Open browser DevTools (F12)
   - Check Console tab for errors
   - Look for failed network requests

2. **Verify Spec Loads**
   - Check Network tab in DevTools
   - Find request to `openapi.yaml`
   - Verify it returns 200 OK
   - Check response content

3. **Test with Different Tool**
   - Try Swagger UI: https://editor.swagger.io/
   - Paste the spec URL or content
   - Verify it renders correctly

4. **Regenerate Spec**
   ```bash
   # Run integration tests to regenerate
   ./gradlew clean test
   
   # Copy to main resources if needed
   cp src/test/resources/spec/openapi.yaml src/main/resources/api-docs/openapi.yaml
   ```

## Common Mistakes

❌ **Wrong URL** - Make sure you're accessing `/api-docs` not `/docs` or other paths

❌ **Old Port** - We use port 5051, not 5050 or 8080 (8080 is Docker Compose only)

❌ **Application Not Running** - Ensure `./gradlew run` is active

❌ **Build Artifacts** - Old JAR might be cached, run `./gradlew clean`

## Spec File Locations

- **Runtime:** `src/main/resources/api-docs/openapi.yaml` (671 lines)
- **Test-Generated:** `src/test/resources/spec/openapi.yaml` (671 lines)
- **Both should be identical**

## Need Help?

If none of these solutions work:
1. Share your browser console errors
2. Share the output of `curl http://localhost:5051/api-docs/openapi.yaml | head -50`
3. Verify the application is running on port 5051
4. Check if there are any proxy/firewall issues
