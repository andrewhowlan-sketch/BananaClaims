# Banana Claims Book GUI

The Book GUI is an optional, server-generated management interface. It does not require a client mod and does not replace commands; both interfaces call the same claim services.

## Opening the Book

```text
/claim
/claim book
/claim book <claim>
```

## Pages

1. Claim selector and home page
2. Claim overview and editable information
3. Members, subowners, and invitation summary
4. Protection flags
5. Enter/leave notification appearance
6. BlueMap appearance
7. Claim tools and preview controls
8. Ownership, transfer, leave, and deletion
9. Incoming invitations
10. Member management
11. Subowner management

## Role-Aware Access

- Owners receive all normal claim-management controls.
- Subowners receive only actions permitted by the role model.
- Members receive read-only information, preview, and leave controls.
- Destructive actions use confirmation pages.
- Hidden actions are protected by short-lived, player-bound book session tokens.

## Vanilla Book Limitations

Written books support clickable commands and page navigation but not text boxes, sliders, or color pickers. Actions requiring free text use a clickable prefilled command. Boolean flags and presets can be changed entirely inside the book.

## Security

The book never trusts a button alone. Each click is revalidated against:

- The player-bound session token
- Session expiration
- Current claim existence
- Current role and permission node
- Current member or invitation state

A copied or manually forged internal action command cannot bypass these checks.
