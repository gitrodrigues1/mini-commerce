create table payments (
    order_id uuid primary key,
    sku varchar(64) not null,
    amount numeric(12, 2) not null,
    status varchar(32) not null,
    reason varchar(255),
    created_at timestamp not null
);

create table processed_events (
    event_id uuid primary key,
    processed_at timestamp not null
);
