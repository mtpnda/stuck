# LLM Model Setup & Next Steps

## 1. Current App Status
- The Contexts application is **fully implemented, tested, and functional**.
- All **51 test cases pass cleanly** (`npm test`), and the production bundle export (`npx expo export --platform ios`) bundles all 1,060 modules with zero errors.
- The base Kanban workflow, local persistence (`AsyncStorage`), Face ID biometric lock, push notifications, and focus timer operate **100% offline with zero external network or model dependencies**.
- As requested, **large LLM model weight downloads have been skipped**.

---

## 2. When You Are Ready: LLM Model Download Options

If you wish to enable on-device AI features (e.g., natural language task extraction, automatic to-do breakdown, and cognitive load advisory), choose one of the following approaches:

### Approach A: Local On-Device GGUF Model (Recommended for Offline Privacy)

#### Step 1: Download the Model Weights
We recommend lightweight instruction-tuned models optimized for mobile devices with 4-bit quantization (Q4_K_M):

1. **Option 1: Llama 3.2 1B Instruct (High quality, ~750 MB)**
   ```bash
   mkdir -p rn/assets/models
   curl -L -o rn/assets/models/Llama-3.2-1B-Instruct-Q4_K_M.gguf \
     https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf
   ```

2. **Option 2: Qwen 2.5 0.5B Instruct (Ultra-compact, ~390 MB)**
   ```bash
   mkdir -p rn/assets/models
   curl -L -o rn/assets/models/Qwen2.5-0.5B-Instruct-Q4_K_M.gguf \
     https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf
   ```

3. **Option 3: SmolLM2 1.7B Instruct (Balanced, ~1.0 GB)**
   ```bash
   mkdir -p rn/assets/models
   curl -L -o rn/assets/models/SmolLM2-1.7B-Instruct-Q4_K_M.gguf \
     https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF/resolve/main/smollm2-1.7b-instruct-q4_k_m.gguf
   ```

#### Step 2: Install Mobile LLM Runtime
Inside `rn/`:
```bash
npm install llama.rn
```
Add the plugin to `app.json`:
```json
{
  "expo": {
    "plugins": [
      "llama.rn"
    ]
  }
}
```

#### Step 3: Rebuild Native App
Because `llama.rn` includes native C++ / Metal acceleration bindings, run:
```bash
npx expo prebuild --platform ios
npx expo run:ios
```

---

### Approach B: Host CLI Relay via `agy` (Zero Download on iPhone)
If you do not want to download large models onto the iPhone, you can relay prompts to the host machine running the Antigravity (`agy`) CLI session:
1. Start the host relay (using your existing authenticated agy session):
   ```bash
   agy --dangerously-skip-permissions
   ```
2. The app communicates with the relay over the local network via Tailscale or local WebSocket/HTTP.
3. Responses are streamed directly into Contexts without storing model weights on the phone.

---

## 3. Summary of Immediate Action Items
1. To run the app as-is:
   ```bash
   cd rn
   npm start
   ```
2. To test the app:
   ```bash
   cd rn
   npm test
   ```
3. To enable AI/LLM:
   Follow the download commands in **Step 1** above when ready.
