---
name: PokedexLab Figma design tokens
description: Colors, typography, shapes and spacing extracted from the Figma styleguide
type: project
---

Extracted from Figma file `ZhswWnNVkMM5mjIYB1M554`, node `913:238`.

**Primary color:** PokedexRed = #DC0A2D

**Grayscale:**
- Gray1 = #212121 (text primary)
- Gray2 = #666666 (text secondary)
- Gray3 = #E0E0E0 (dividers)
- GrayBackground = #EFEFEF (screen background)
- White = #FFFFFF (cards, surface)

**18 Pokémon type colors** in `Color.kt` → `pokemonTypeColor(typeName)`:
Bug #A7B723 · Dark #75574C · Dragon #7037FF · Electric #F9CF30 · Fairy #E69EAC
Fighting #C12239 · Fire #F57D31 · Flying #A891EC · Ghost #70559B · Grass #74CB48
Ground #DEC16B · Ice #9AD6DF · Normal #AAA67F · Poison #A43E9E · Psychic #FB5584
Rock #B69E31 · Steel #B7B9D0 · Water #6493EB

**Typography (Poppins):**
- HeadlineBold 24sp/32sp
- Subtitle1Bold 14sp/16sp · Subtitle2Bold 12sp/16sp · Subtitle3Bold 10sp/16sp
- Body1Regular 14sp · Body2Regular 12sp · Body3Regular 10sp
- CaptionRegular 8sp/12sp

**Shapes:** extraSmall=7dp · small=8dp · medium=12dp · large=16dp · TypeChipShape=10dp

**Spacing:** xxs=2dp · xs=4dp · s=8dp · m=12dp · l=16dp · xl=20dp · xxl=24dp · xxxl=56dp

**Figma node IDs (use `:` not `-`):**
- Styleguide: 913:238 · Components: 913:239
- List screen: 1017:431 · Sort card: 1024:1979 · Detail: 1016:1461

**Why:** Background color of the detail screen is the primary type color of the Pokémon (e.g., Bulbasaur → Grass #74CB48). Type chips use the same color with white text.

**How to apply:** Always use the token names from Color.kt — never hardcode hex values in feature modules.
