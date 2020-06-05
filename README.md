# Feather Kraken

:octopus: Server application for searching flights with variable source airports.

[![CircleCI](https://circleci.com/gh/featherkraken/featherkraken.svg?style=svg)](https://circleci.com/gh/featherkraken/featherkraken)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ingokuba_featherkraken&metric=alert_status)](https://sonarcloud.io/dashboard?id=ingokuba_featherkraken)
[![codecov](https://codecov.io/gh/featherkraken/featherkraken/branch/master/graph/badge.svg)](https://codecov.io/gh/featherkraken/featherkraken)

To be able to use the endpoints, the `tequilaApiKey` system property must be set for [`KiwiUtil.java`](src/main/java/featherkraken/kiwi/control/KiwiUtil.java#L17).

## Airport finder

To be able to search for specific airports using part of the name of the airport or city, there is the endpoint `/airports` which can be reached with a GET request. The input is passed using the query parameter `query` in the URL of the request. For example, an request for searching airports in Amsterdam might be structured as follows:

```
GET /airports?query=Ams
```

## Flight search

The core function of the application is behind the endpoint `/flights`, which translates incoming search queries into a format understandable by the external API and then returns the search result. Search parameters are not passed in the URL of the request, but instead with a POST in the request body, as this makes the search query clearer and easier to document. You can optionally specify a radius for the search, in which case flights from surrounding departure airports are searched for.
