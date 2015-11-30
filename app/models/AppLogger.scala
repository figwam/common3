package models


case class AppLogger (rootid: String,
                   title: String,
                   exception: String,
                   stacktrace: String,
                   req_header: String,
                   req_method: String,
                   req_address: String,
                   req_uri: String)
