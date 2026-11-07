# On-Device Intelligence & SLM Specification

## 1. Objectives & Scope
Stuck enhances mindful cognitive offloading using 100% on-device local intelligence without sending user thoughts to third-party cloud servers:
1. **Natural Language Thought Ingestion**:
   - Parses unstructured input into structured fields (`title`, `startAt`, `todos`).
2. **Context Deconstruction / AI To-Do Breakdown**:
   - Decomposes complex executive thoughts into bite-sized actionable checklists.
3. **Cognitive Load Advisory**:
   - When overloaded (`load > limit`), recommends which thoughts can safely be parked based on cognitive friction.

## 2. On-Device Architecture
Aligned with BioCue's On-Chip SLM Architecture:
- **iOS**: Apple `NaturalLanguage.framework` + Core ML / Metal-accelerated SLM (`Llama-3.2-1B` or `SmolLM2-360M` via MLX Swift).
- **Android**: Google Play Services `ML Kit` Entity Extraction + on-device LiteRT / MediaPipe GenAI.
- **Zero Cloud Leaks**: Zero network transmission; all parsing occurs locally on the neural engine.
