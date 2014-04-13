# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table document (
  id                        bigint not null,
  name                      varchar(255) not null,
  parent_id                 bigint not null,
  sheet_name                varchar(255) not null,
  envelope_id               bigint,
  constraint pk_document primary key (id))
;

create table envelope (
  id                        bigint not null,
  envelope_id               varchar(255) not null,
  email_subject             varchar(255) not null,
  date_sent                 timestamp not null,
  date_last_updated         timestamp not null,
  user_id                   bigint,
  status                    varchar(7) not null,
  constraint ck_envelope_status check (status in ('CREATED','SENT')),
  constraint uq_envelope_envelope_id unique (envelope_id),
  constraint pk_envelope primary key (id))
;

create table recipient (
  email                     varchar(255) not null,
  name                      varchar(255) not null,
  recipient_status          varchar(12) not null,
  envelope_id               bigint,
  constraint ck_recipient_recipient_status check (recipient_status in ('NOT_RECIEVED','RECIEVED','SIGNED','DECLINED')))
;

create table user_details (
  id                        bigint not null,
  email                     varchar(255) not null,
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  access_token              varchar(255) not null,
  constraint uq_user_details_email unique (email),
  constraint pk_user_details primary key (id))
;

create sequence document_seq;

create sequence envelope_seq;

create sequence user_details_seq;

alter table document add constraint fk_document_envelope_1 foreign key (envelope_id) references envelope (id);
create index ix_document_envelope_1 on document (envelope_id);
alter table envelope add constraint fk_envelope_user_2 foreign key (user_id) references user_details (id);
create index ix_envelope_user_2 on envelope (user_id);
alter table recipient add constraint fk_recipient_envelope_3 foreign key (envelope_id) references envelope (id);
create index ix_recipient_envelope_3 on recipient (envelope_id);



# --- !Downs

drop table if exists document cascade;

drop table if exists envelope cascade;

drop table if exists recipient cascade;

drop table if exists user_details cascade;

drop sequence if exists document_seq;

drop sequence if exists envelope_seq;

drop sequence if exists user_details_seq;

