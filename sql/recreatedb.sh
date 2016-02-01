#!/bin/bash

/Applications/Postgres.app/Contents/Versions/9.4/bin/psql -f /Users/alex/workspace/sport/portal3/common/sql/drop-all.sql gymix
/Applications/Postgres.app/Contents/Versions/9.4/bin/psql -f /Users/alex/workspace/sport/portal3/common/sql/create-all.sql gymix
/Applications/Postgres.app/Contents/Versions/9.4/bin/psql -f /Users/alex/workspace/sport/portal3/common/sql/insert-all.sql gymix