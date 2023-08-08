// hexpat for Stonks2 Core
// Supported binary versions:
// OfferV1: 0x00000001

struct UUID {
    u8 data[16];
};

struct UTF {
    be u16 length;
    char data[length];
};

enum _Version : u32 {
    NULL = 0x00,
    V1 = 0x01
};

using Version = be _Version;

struct OfferV1 {
    UUID offerId;
    UUID offererId;
    UTF productId;
    UTF prouctType;
    be u32 total, claimed, filled;
    be double pricePerUnit;
};

struct Offer {
    Version version;
    if (version == _Version::NULL) break;
    if (version == _Version::V1) OfferV1;
};

Offer offers[1000] @ 0x00;
