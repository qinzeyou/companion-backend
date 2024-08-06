-- 用户表
create table user
(
    id          bigint(11) auto_increment comment '主键'
        primary key,
    username    varchar(256)                       null comment '用户昵称',
    userAccount varchar(256)                       null comment '账号',
    password    varchar(512)                       not null comment '密码',
    phone       varchar(128)                       null comment '电话',
    avatarUrl   varchar(1024)                      null comment '用户头像',
    gender      tinyint                            null comment '性别',
    email       varchar(512)                       null comment '邮箱',
    userStatus  int      default 0                 not null comment '状态 0-正常',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    userRole    int      default 0                 not null comment '用户角色 0 普通用户 1 管理员',
    tags        varchar(1024)                      null comment '标签 json 列表',
    profile     varchar(512)                       null comment '描述'
) comment '用户' charset = utf8mb4;

-- 队伍表
create table team
(
    id          bigint(11) auto_increment comment '主键'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint(11)                         null comment '创建人id（队长id）',
    status      int      default 0                 not null comment '队伍状态：0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       not null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '队伍表';

-- 用户队伍关系表
create table user_team
(
    id         bigint(11) auto_increment comment '主键'
        primary key,
    userId     bigint(11)                         not null comment '用户id',
    teamId     bigint(11)                         not null comment '队伍id',
    joinTime   datetime                           not null comment '过期时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍表';

-- 标签表
CREATE TABLE `tag`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tagName`    varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '标签名称',
    `userId`     bigint(20)                                                       DEFAULT NULL COMMENT '创建者id',
    `parentId`   bigint(20)                                                       DEFAULT NULL COMMENT '父标签id',
    `userNumber` int(11)                                                          DEFAULT '0' COMMENT '使用人数',
    `status`     tinyint(4)                                                       DEFAULT '1' COMMENT '标签状态 0-不可见 1-可见',
    `textColor`  varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '标签文字颜色',
    `color`      varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL COMMENT '背景颜色',
    `createTime` datetime                                                         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime                                                         DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   tinyint(4)                                                       DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniIdx_tagName` (`tagName`),
    KEY `idx_userId` (`userId`)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_520_ci;

create index idx_userId
    on tag (userId);