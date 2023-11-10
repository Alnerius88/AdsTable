-- liquibase formatted sql
-- changeset rhont:1

create table image_details
(
    id_image        int4 generated by default as identity,
    path_to_file    varchar(255),
    file_size       int8,
    media_type      varchar(255),
    path_hard_store varchar(255),
    extension       varchar(255),
    primary key (id_image)
);
create table roles
(
    id   int4 generated by default as identity,
    name varchar(255),
    primary key (id)
);
create table users
(
    id_image   int4,
    primary key (id),
    id         int4 generated by default as identity,
    first_name varchar(255),
    last_name  varchar(255),
    password   varchar(255),
    phone      varchar(255),
    username   varchar(255)
);

create table user_roles
(
    user_id int4 not null,
    role_id int4 not null
);
create table ads
(
    pk          int4 generated by default as identity,
    description varchar(255),
    price       int4,
    title       varchar(255),
    id          int4,
    id_image    int4,
    primary key (pk)
);
create table comments
(
    comment_id int4 generated by default as identity,
    created_at int8,
    text       varchar(255),
    pk         int4,
    id         int4,
    primary key (comment_id)
);

alter table if exists ads
    add constraint FK15urvik8jofutsye5a5fmobuw foreign key (id) references users;

alter table if exists ads
    add constraint FKcdouh9ee0f33hkvobg5mknt5a foreign key (id_image) references image_details;

alter table if exists comments
    add constraint FKp16ci16vr23rr2g8aooracq9m foreign key (pk) references ads;

alter table if exists comments
    add constraint FKfro76752aptbhmbmqya70hrga foreign key (id) references users;

alter table if exists user_roles
    add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 foreign key (role_id) references roles;

alter table if exists user_roles
    add constraint FKhfh9dx7w3ubf1co1vdev94g3f foreign key (user_id) references users;

alter table if exists users
    add constraint FK7tgees22qv9gjd01pe6ofmm46 foreign key (id_image) references image_details;

-- changeset rhont:2
INSERT INTO roles(name)
VALUES ('USER'),
       ('ADMIN');

Insert Into users(first_name, last_name, phone, username)
VALUES ('Евгений', 'Белых', '+79117348358', 'user@gmail.com'),
       ('Александр', 'Лапутин', '+79117348358', 'admin@gmail.com'),
       ('Враг', 'Врагович', '+79117348358', 'enemy@gmail.com');

insert into user_roles(user_id, role_id)
VALUES (1,1),
       (2,2),
       (3,1);







