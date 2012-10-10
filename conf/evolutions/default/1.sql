# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ticket_m (
  id                        bigint not null,
  label                     varchar(255),
  description               varchar(255),
  price                     decimal(38),
  start                     timestamp,
  end                       timestamp,
  constraint pk_ticket_m primary key (id))
;

create sequence ticket_m_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ticket_m;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ticket_m_seq;

