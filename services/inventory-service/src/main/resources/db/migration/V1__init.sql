create table stock_items (
    sku varchar(64) primary key,
    available_qty int not null,
    reserved_qty int not null
);

create table reservations (
    order_id uuid primary key,
    sku varchar(64) not null,
    quantity int not null,
    status varchar(32) not null,
    created_at timestamp not null
);

create table processed_events (
    event_id uuid primary key,
    processed_at timestamp not null
);

insert into stock_items (sku, available_qty, reserved_qty) values
    ('BOOK-01', 20, 0),
    ('MUG-02', 5, 0),
    ('SKU-FAIL-PAY', 10, 0),
    ('SKU-FAIL-STOCK', 0, 0);
