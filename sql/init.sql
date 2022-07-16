create database if not exists dcs_alarm default character set = 'utf8';
use dcs_alarm;


create table alarm_class
(
    id      bigint(19)           not null
        primary key,
    name    varchar(256)         not null comment '报警类型名称',
    code    varchar(256)         not null comment '报警类别编码;0：机械，1：电气，2：工艺，3：安全，4：环保',
    deleted tinyint(1) default 0 not null
)
    comment '报警分类';

create table alarm_history
(
    id                bigint(19)                           not null
        primary key,
    deleted           tinyint(1) default 0                 not null,
    alarm_context     longtext                             not null comment '报警内容',
    create_time       datetime   default CURRENT_TIMESTAMP not null comment '报警时间',
    push_status       varchar(30)                          not null comment '推送状态',
    device_no         varchar(256)                         null comment '设备编码',
    ref_alarm_rule_id bigint(19)                           not null comment '报警规则id'
)
    comment '报警历史记录';

create index alarm_history_ref_alarm_rule_id_index
    on alarm_history (ref_alarm_rule_id);

create table alarm_rule
(
    id             bigint(19)                 not null
        primary key,
    alarm_mode     varchar(32)                not null comment '报警模式：TRIG/LIM',
    alarm_sub_mode varchar(32)                not null comment 'HIGH/LOW
RISE/DOWN',
    is_audio       tinyint(1)   default 0     null comment '是否语音报警',
    deleted        tinyint(1)   default 0     null comment '是否删除',
    alarm_class_id bigint(19)                 null comment '报警类别关联',
    point_id       bigint(19)                 null comment '报警点位id',
    alarm_group    varchar(32)                not null comment '报警组：ktj，sl，sc，zc,yr
',
    limite_value   decimal(19, 8)             not null comment '限制值',
    alarm_temple   longtext                   not null comment '推送模板',
    is_wx_push     tinyint(1)   default 0     not null comment '是否微信推送',
    alarm_interval varchar(256) default '300' not null comment '报警间隔'
)
    comment '报警规则';

create table alarm_rule_switch_map
(
    id                bigint(19)           not null
        primary key,
    deleted           tinyint(1) default 0 not null,
    ref_alarm_rule_id bigint(19)           not null comment '报警规则id',
    ref_switch_id     bigint(19)           not null comment '开关id'
)
    comment '规则开关关联：规则是否生效';

create table device
(
    id          bigint(19)           not null
        primary key,
    device_no   varchar(128)         null comment '设备编码，需要填写和兰亮设备编码对应的数据',
    device_name varchar(256)         not null comment '设备名称',
    deleted     tinyint(1) default 0 not null,
    process     varchar(64)          not null comment 'gongxun:sl,sc,zc'
)
    comment '设备表';

create table point
(
    id            bigint(19)           not null
        primary key,
    deleted       tinyint(1) default 0 not null,
    tag           varchar(512)         not null comment '位号',
    name          varchar(512)         not null comment '位号名称',
    ref_device_id bigint(19)           not null comment '引用的设备id',
    node_code     varchar(256)         not null comment 'iot节点编码'
)
    comment '点号表';

create table switch
(
    id           bigint(19)           not null
        primary key,
    deleted      tinyint(1) default 0 not null,
    name         varchar(256)         not null comment '开关名称',
    switch_logic varchar(30)          not null
)
    comment '设备是否需要报警的开关规则表';

create table switch_rule
(
    id            bigint(19)           not null
        primary key,
    deleted       tinyint(1) default 0 not null,
    ref_switch_id bigint(19)           not null comment '关联的开关id',
    rule_code     varchar(64)          not null comment '规则：大于，等于。。。',
    point_id      bigint(19)           not null comment '点位id',
    limit_value   decimal(19, 8)       not null comment '限制值'
)
    comment '开关规则';

create table system_config
(
    id           bigint(19)           not null
        primary key,
    name         varchar(256)         not null comment '属性名称',
    code         varchar(256)         not null comment '属性编码
',
    deleted      tinyint(1) default 0 not null,
    value        longtext             not null comment '属性值
',
    config_group varchar(256)         not null comment '属性组别'
);





INSERT INTO alarm_class (id, name, code, deleted)
VALUES
(1,'机械',0,0),
(2,'电气',1,0),
(3,'工艺',2,0),
(4,'安全',3,0),
(5,'环保',4,0);


INSERT INTO `system_config` VALUES (1, 'IOTUrl', 'iotUrl', 0, 'http://192.168.145.207/iot', 'collector');
INSERT INTO `system_config` VALUES (2, '内部Url', 'innerUrl', 0, 'http:/127.0.0.1:8770', 'collector');
INSERT INTO `system_config` VALUES (3, '数据来源', 'dataSource', 0, 'iot', 'collector');
INSERT INTO `system_config` VALUES (4, '持续报警时间', 'continueAlarmSec', 0, '300', 'wx');
INSERT INTO `system_config` VALUES (5, '推送间隔时间', 'pushIntervalSec', 0, '60', 'wx');
INSERT INTO `system_config` VALUES (6, '推送群名', 'dePartment', 0, '桐庐测试群', 'wx');
INSERT INTO `system_config` VALUES (7, '语音播报速度', 'audioRate', 0, '1', 'audio');
INSERT INTO `system_config` VALUES (8, '微信推送Url', 'wxpushUrl', 0, 'http://127.0.0.1:8080/HS_Push/SendMsgAction', 'wx');
INSERT INTO `system_config` VALUES (9, '公司编码', 'companyCode', 0, '3106', 'company');
INSERT INTO `system_config` VALUES (10, '公司名称', 'companyName', 0, '桐庐红狮', 'company');
INSERT INTO `system_config` VALUES (11, 'iot节点编码,号隔开', 'nodeCodes', 0, 'dcs', 'collector');
INSERT INTO `system_config` VALUES (12, '工序,号隔开', 'processName', 0, '生料-sl,烧成-sc,制成-zc', 'process');

