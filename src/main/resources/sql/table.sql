--Oracle
create table t_mscheduler_lock(
  method varchar(200) primary key,
  appid varchar(50),
  ip varchar(20),
  startTime number(20)
);
comment on table t_mscheduler_lock is 'spring定时计划锁表';
comment on column t_mscheduler_lock.method is '方法';
comment on column t_mscheduler_lock.appid is '应用id';
comment on column t_mscheduler_lock.ip is 'ip';
comment on column t_mscheduler_lock.startTime is '锁定开始时间戳';