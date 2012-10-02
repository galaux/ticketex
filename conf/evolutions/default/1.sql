# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table task (
  id                        bigint not null,
  label                     varchar(255),
  constraint pk_task primary key (id))
;

create table ticket (
  id                        bigint not null,
  label                     varchar(255),
  description               varchar(255),
  price                     decimal(38),
  start                     timestamp,
  end                       timestamp,
  constraint pk_ticket primary key (id))
;

create sequence task_seq;

create sequence ticket_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists task;

drop table if exists ticket;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists task_seq;

drop sequence if exists ticket_seq;

