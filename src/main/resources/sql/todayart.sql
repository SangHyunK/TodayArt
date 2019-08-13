# TodayArt Scheme v1.0

DROP DATABASE IF EXISTS `todayart`;

CREATE DATABASE IF NOT EXISTS `todayart`
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

CREATE USER IF NOT EXISTS 'art_master'@'%' IDENTIFIED BY 'art12#$';

GRANT ALL PRIVILEGES ON todayart.* To 'art_master'@'%';

USE `todayart`;

DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `artist`;
DROP TABLE IF EXISTS `artist_level`;
DROP TABLE IF EXISTS `member_role`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `privilege`;
DROP TABLE IF EXISTS `role_privilege`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `shipping`;
DROP TABLE IF EXISTS `board_category`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `wishlist`;
DROP TABLE IF EXISTS `member_address`;
DROP TABLE IF EXISTS `order_detail`;
DROP TABLE IF EXISTS `mileage_invoice`;
DROP TABLE IF EXISTS `mileage_statement`;
DROP TABLE IF EXISTS `payment`;
DROP TABLE IF EXISTS `cart`;
DROP TABLE IF EXISTS `account`;
DROP TABLE IF EXISTS `file`;

/* 회원 테이블 */
CREATE TABLE IF NOT EXISTS `member` (
	`member_id` INT AUTO_INCREMENT,
	`email` VARCHAR(255) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`nickname` VARCHAR(255) NOT NULL,
	`username` VARCHAR(255) NULL,
	`reg_dated`	DATETIME NOT NULL DEFAULT NOW(),
	`phone`	VARCHAR(255) NULL,
	`authority`	INT(1) NOT NULL DEFAULT 0 COMMENT "0: 일반, 1: 판매자, 2: 관리자",
	`last_connect_dated` DATETIME NULL,
	`expired` INT(1) NOT NULL DEFAULT 0 COMMENT "0: 미탈퇴, 1: 탈퇴",
	`expired_dated`	DATETIME NULL,
	`email_checked`	INT(1) NOT NULL DEFAULT 0 COMMENT "0: 미인증, 1: 인증",
    CONSTRAINT member_pk_id PRIMARY KEY(member_id),
    CONSTRAINT member_uk_email UNIQUE KEY(email)
);

/* 작가회원 */
CREATE TABLE IF NOT EXISTS `artist` (
	`artist_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`member_id` INT NOT NULL,
	`artist_desc` TEXT NOT NULL,
    `profile_id` INT NULL,
	`file_id` INT NOT NULL,
	`adm_product_desc` TEXT NOT NULL,
	`adm_check`	INT(1) NOT NULL DEFAULT 0 COMMENT "0: 미승인, 1: 승인",
	`artist_level_id` INT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
);

/* 작가등급 */
CREATE TABLE IF not exists `artist_level` (
	`artist_level_id` int not null,
	`level` varchar(10)	not null default "Bronze",
	`commission` int not null default "15"
);

CREATE TABLE IF not exists `member_role` (
	`m_id`	int	not null,
	`r_id`	VARCHAR(255)	not null
);

CREATE TABLE IF not exists `role` (
	`r_id`	VARCHAR(255)	not null,
	`name`	VARCHAR(255)	null,
	`desc`	VARCHAR(255)	null
);

CREATE TABLE IF not exists `privilege` (
	`p_id`	VARCHAR(255)	not null,
	`name`	VARCHAR(255)	null,
	`desc`	VARCHAR(255)	null
);

CREATE TABLE IF not exists `role_privilege` (
	`r_id`	VARCHAR(255)	not null,
	`p_id`	VARCHAR(255)	not null
);

/* 상품 카테고리 */
CREATE TABLE IF not exists `category` (
	`category_id` int not null,
	`category_name` varchar(255) not null
);

/* 상품 */
CREATE TABLE IF not exists `product` (
	`product_id` int not null,
	`product_name` varchar(255) not null,
	`artist_id` int not null,
	`product_size` varchar(255) not null,
	`product_price`	int	not null,
	`thumbnail_id` int not null,
	`is_delete` int not null default 0 comment "0: 미삭제, 1:삭제",
	`delete_dated` datetime null,
	`enroll_dated` datetime not null default now(),
	`update_dated` datetime null,
	`category` int not null,
	`is_sold_out` int not null default 0 comment "0: 판매가능, 1: 품절",
	`remain` int not null default 1,
	`count_cart` int not null default 0,
	`count_wishlist` int not null default 0,
    `shipping_fee` int not null
);

/* 주문 */
CREATE TABLE IF not exists `order` (
	`order_id` int not null,
	`member_id` int not null,
	`order_dated` datetime not null default now(),
	`total_price` int not null,
	`shipping_fee` int not null,
	`cart_id` int not null,
    `shipping_id` int not null
    
);

/* 배송 */
CREATE TABLE IF not exists `shipping` (
	`shipping_id` int not null,
	`courier` varchar(255) not null,
	`order_id` int not null,
	`shipping_dated` datetime null,
	`shipping_fee` int null,
	`consignee` varchar(255) not null,
	`receive_addr` varchar(255) not null,
	`consignee_phone` int not null,
	`artist_id` int not null,
	`member_id` int not null
);

/* 게시판 카테고리 */
CREATE TABLE IF not exists `board_category` (
	`board_id` int not null,
	`board_name` varchar(255) null
);

/* 게시글 */
CREATE TABLE IF not exists `article` (
	`article_id` int not null,
	`board_id` int not null,
	`writter_id` int not null,
	`write_dated` datetime not null default now(),
	`update_dated` datetime null,
	`title` varchar(255) not null,
	`content` text not null,
	`group` int null,
    `depth` int not null default 0,
	`reply_order` int null,
	`is_deleted` int not null default 0 comment "0:미삭제, 1:삭제",
	`delete_dated` datetime null,
	`is_hidden`	int not null default 0 comment "0:공개글, 1:비밀글",
    `password` varchar(255) null,
    `is_reply` int not null default 0 comment "0:미답변, 1:답변",
    `views` int not null default 0,
    `product_id` int null
	
);
/* 찜하기 */
CREATE TABLE IF not exists `wishlist` (
	`member_id` int not null,
	`product_id` int not null,
	`enroll_dated` datetime null
);

/* 주소 */
CREATE TABLE IF not exists `member_address` (
	`address_id` int not null,
	`member_id`	int	not null,
	`post_number` int not null,
	`address` varchar(255) not null,
	`address_detail` varchar(255) null,
	`main_address` int not null default 0 comment "0:일반배송지, 1:대표배송지"
);

/* 주문 상세 */
CREATE TABLE IF not exists `order_detail` (
	`order_id` int not null,
	`product_id` int not null,
	`product_price` int not null,
	`quantity`	int	not null,
	`status` varchar(255) not null	
);

/* 판매대금명세 */
CREATE TABLE IF not exists `mileage_invoice` (
	`mileage_id` int not null,
	`artist_id` int not null,
	`exchanged_date` int null,
	`exchanged_mileage` int null,
	`exchanged_cash` int null,
	`bank_name` int null,
	`deposit_account` int null,
	`balance` int null
);


/* 판매대금 현황 */
CREATE TABLE IF not exists `mileage_statement` (
	`trade_id`	int	not null,
	`artist_id`	int	not null,
	`order_id`	int	not null,
	`balance`	int	null,
	`income`	int	null,
	`commission_rate`	int	null,
	`commission`	int	null,
	`deposite_mileage`	int	null,
	`invoice_id`	int	null,
	`withdraw_mileage`	int	null,
	`create_dated`	int	null
);

/* 결제 */
CREATE TABLE IF not exists `payment` (
	`payment_id` int not null,
	`order_id` int not null,
	`pay_dated` datetime not null default now(),
	`pay_method` varchar(255) not null,
	`card_number` varchar(255) null,
	`pay_price`	int	null,	
	`total_price` int null,
	`status` char(13) null
);

/* 장바구니 */
CREATE TABLE IF not exists `cart` (
	`cart_id` int not null,
	`member_id` int	not null,
	`product_id` int not null,
	`enroll_dated` datetime null,		
	`is_stock` int not null default 1 comment "0:재고없음, 1:재고있음"
);

/* 계좌 */
CREATE TABLE IF not exists `account` (
	`artist_id` int not null,
    `account_number` varchar(255) not null,
    `bank` varchar(255) not null,
    `main_account` int not null default 0 comment "0:일반계좌, 1:대표계좌"
);


/* 파일 */
CREATE TABLE IF not exists `file` (
	`file_id` int not null,
    `file_originname` varchar(255) not null,
    `file_name` varchar(255) not null,
    `file_size` int not null default 0,
    `file_type` varchar(255) null,
    `file_reg_dated` datetime not null default now(),
    `file_ip` varchar(255) null,
    `member_id` int not null
);

select * from member;

update member set expired = 1, expired_dated = now() where member_id = 3000;