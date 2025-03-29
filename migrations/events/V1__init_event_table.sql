create table event_stream (
    id           uuid  not null primary key,
    aggregate_id uuid  not null,
    version      int   not null,
    data         jsonb not null,
    unique(aggregate_id, version)
);