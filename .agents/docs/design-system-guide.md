# Design System Guide

## 1. Golden Rules
1. **Never use raw values in screens**:
   - ❌ No raw colors (e.g. `color="#16202C"` or `rgba(...)`)
   - ❌ No hardcoded font styles (`fontSize: 16`, `fontWeight: "600"`)
   - ❌ No `StyleSheet.create` inside screen components
   - ❌ No numeric padding/margin properties (`padding: 16`)
2. **Use token names only**:
   - ✅ Colors: `color="mute"`, `color="ink"`, `color="danger"`
   - ✅ Spacing: `padding="l"`, `marginVertical="m"`, `gap="s"`
   - ✅ Typography: `<Text variant="title">`, `<Text variant="small">`
   - ✅ Cards: `<Card variant="item">`, `<Card variant="column">`
   - ✅ Buttons: `<Button kind="primary">`, `<Button kind="plain">`

## 2. Changing the Visual Identity
To update the look of the entire application:
1. Open `rn/src/design/tokens.js`.
2. Edit the raw palette or the semantic light/dark token mappings.
3. Every component automatically adopts the new visual tokens.

## 3. Adding New Tokens or Variants
When adding a new design element:
1. Add the token in `tokens.js` (under `spacing`, `borderRadii`, `cardVariants`, `buttonVariants`, or `textVariants`).
2. If adding a semantic color, add it to both `light` and `dark` themes.
3. Run `npm test` inside `rn` to verify that `__tests__/design.test.js` passes.
