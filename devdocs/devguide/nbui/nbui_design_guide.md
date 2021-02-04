# NBUI Design Guide

General design settings for new NBUI work:

```css
{
 font-family: "Mulish, sans-serif"
}
```

## Dashboard Views

- Use layered sections with clear headings.
- Populate each section with cards that layout left to right.
  - Show all cards, or allow the view to scroll horizontally with arrows.
  - If scrolling horizontally, show the number of elements and allow the
    user to show them all in a left-to-right, top-to-bottom layout.
  - Each card should show top-level stats and state of the element.
  - Each card should have useful hover info for key details.
  - Each card should have clickable links to zoom into element details on
    different views where available.
- For actions which can affect the elements in a section, use a clearly
 labeled button across the top right of the section, horizontally on the
 same level as the section heading.
- Add sections which offer help for the user where possible, including
  - links to docs, guides, or related services or integrations
  - links to videos with preview


