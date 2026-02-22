---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/views/overview
---

# Overview

## Content

The `views` module provides support for the Android view system.

## XML attributes

An extensive XML styling framework is available. See [`attrs.xml`](https://github.com/patrykandpatrick/vico/blob/stable/vico/views/src/main/res/values/attrs.xml) for an attribute list.

Of course, XML styling has its limitations; more advanced customization is performed programmatically. Fully programmatic use is also supported, but the XML styling framework provides preferable, environment-aware defaults. Thus, it’s recommended that you use XML styling whenever possible and complement it with programmatic customization when necessary. This is achieved primarily via the `copy` functions.
