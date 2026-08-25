# Architecture

```text
HTTP
 |
 v
OfferController
 |
 v
OfferService -----------------> FeatureToggleService
 |                                  | local
 |                                  ` Unleash
 v
OfferRepository
 |
 v
PostgreSQL
```

The important boundary is `FeatureToggleService`: domain/service code knows feature names and contexts, but not the remote feature-management SDK.

The flag changes behavior, not deployment topology. Both code paths are deployed together and the rollout decision happens per request.
