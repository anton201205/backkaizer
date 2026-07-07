create extension if not exists pgcrypto;

create table if not exists usuarios (
  id bigserial primary key,
  email varchar(190) not null unique,
  password_hash varchar(100) not null,
  role varchar(30) not null default 'USER',
  created_at timestamptz not null default now(),
  nombre varchar(100),
  telefono varchar(20),
  direccion varchar(200),
  distrito varchar(100),
  dni varchar(8)
);

create table if not exists pedidos (
  id bigserial primary key,
  usuario_id bigint references usuarios(id) on delete set null,
  estado varchar(30) not null default 'CREADO',
  total numeric(12,2) not null default 0 check (total >= 0),
  created_at timestamptz not null default now(),
  direccion_envio text,
  subtotal numeric(10,2),
  igv numeric(10,2),
  envio numeric(10,2),
  costo_envio numeric(10,2) default 0,
  distrito varchar(100),
  metodo_pago varchar(20) check (metodo_pago in ('card','qr'))
);

create table if not exists productos (
  id bigserial primary key,
  nombre varchar(255) not null,
  descripcion varchar(2000) not null,
  precio numeric(12,2) not null check (precio > 0),
  image_url varchar(500) not null,
  stock int not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  categoria varchar(100) not null default 'General',
  especificaciones jsonb not null default '{}'::jsonb
);

create index if not exists idx_pedidos_usuario_id on pedidos (usuario_id);

create table if not exists pedido_items (
  id bigserial primary key,
  pedido_id bigint not null references pedidos(id) on delete cascade,
  producto_id bigint not null references productos(id) on delete restrict,
  cantidad int not null check (cantidad > 0),
  precio_unitario numeric(12,2) not null check (precio_unitario > 0),
  subtotal numeric(12,2) generated always as (cantidad * precio_unitario) stored
);

create index if not exists idx_pedido_items_pedido_id on pedido_items (pedido_id);

insert into usuarios (email, password_hash, role)
values ('admin@kaizer.tech', crypt('Admin12345!', gen_salt('bf')), 'ADMIN')