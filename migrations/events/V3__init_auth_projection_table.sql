create table auth.user (
    id       uuid not null primary key,
    username text not null,
    unique(id),
    unique(username)
);