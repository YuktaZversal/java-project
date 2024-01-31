# Sync-Eqplus

This project is used to keep QSS4 and QuoddSupport users and entitlements in sync.
Eqplus users are added and managaed in quoddsupport. They are then synced in Qss4 for reporting.
This project sends daily mail alerts providing information regarding mismatched details of users and firms of eqplus, and entitlements. 

## Table of Contents
- [Architecture](#architecture)
- [API Info](#api-info)
  - [URL Scheme](#url-scheme)
- [Authorization](#authorization)
- [Setup](#setup)
- [References](#references)

## Architecture

## API info

### URL Scheme

#### Quodd-Support

`http://<DnsName>/api/`

#### QSS4

`https://<DnsName>/internal/`

### Routes

#### Quodd-Support

`GET` `/UpstreamApi`

`GET` `/EntitlementApi`

`GET` `/FirmApi`

`GET` `/SeatApi`

`GET` `/SeatApi`


#### QSS4

`GET` `/internal/syncqs/service/list`

`GET` `/internal/syncqs/product/list`

`GET` `/internal/syncqs/user/list`

`GET` `/internal/syncqs/productservice/list`

`GET` `/internal/syncqs/firm/list`

`GET` `/syncqs/product/:pid/servicelink/list`

`POST` `/syncqs/service/add`

`POST` `/syncqs/product/add`

`POST` `/syncqs/product/:pid/servicelink/add/:sid`

`POST` `/syncqs/user/seat`

`POST` `/syncqs/user/update`

`POST` `/syncqs/firm/add`

`POST` `/syncqs/cid/:id/product/:products`

## Authorization
Valid Quodd **JWT (JSON Web Token)** with entitled services included in claims is required on each request.

## Setup

1. Firstly git clone the repo
```
git clone https://github.com/financeware/sync-eqplus.git
```

2. Import as gradle project 

3. Import quodd commmon project

4. Update settings file, path to point to quodd common


### Build

Create build using Gradle tasks


## References
- https://jwt.io/introduction
