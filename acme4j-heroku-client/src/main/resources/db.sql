create table userAccounts(
    id serial not null primary key,
    keyPair text not null
);

create table certificates(
    id serial not null primary key,
    domain text not null,
    aliases text not null default '',
    expires timestamptz not null,
    keyPair text not null default '',
    certificate text not null default '',
    certificateChain text not null default ''
);

