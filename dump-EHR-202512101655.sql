-- MySQL dump 10.13  Distrib 9.5.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: EHR
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '8d1176a6-c5af-11f0-bf29-2efc6507f1a6:1-396';

--
-- Table structure for table `allergy`
--

DROP TABLE IF EXISTS `allergy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `allergy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` bigint NOT NULL,
  `reaction` varchar(255) DEFAULT NULL,
  `severity` varchar(255) DEFAULT NULL,
  `substance` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `allergy_user_FK` (`patient_id`),
  CONSTRAINT `allergy_user_FK` FOREIGN KEY (`patient_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allergy`
--

LOCK TABLES `allergy` WRITE;
/*!40000 ALTER TABLE `allergy` DISABLE KEYS */;
INSERT INTO `allergy` VALUES (1,1,'Rashes','High','Penicillin'),(2,1,'Itchiness','Low','Grass'),(3,3,'Swelling','High','Penicillin');
/*!40000 ALTER TABLE `allergy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  `field_changed` varchar(255) DEFAULT NULL,
  `change_made` varchar(255) DEFAULT NULL,
  `doctor_id` int DEFAULT NULL,
  `log_id` int DEFAULT NULL,
  `performed_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `audit_log_user_FK` (`user_id`),
  CONSTRAINT `audit_log_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_history`
--

DROP TABLE IF EXISTS `medical_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL,
  `diagnosis` varchar(255) DEFAULT NULL,
  `frequency` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `prescribe_medication` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `medical_history_user_FK` (`patient_id`),
  KEY `medical_history_user_FK_1` (`doctor_id`),
  CONSTRAINT `medical_history_user_FK` FOREIGN KEY (`patient_id`) REFERENCES `user` (`id`),
  CONSTRAINT `medical_history_user_FK_1` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_history`
--

LOCK TABLES `medical_history` WRITE;
/*!40000 ALTER TABLE `medical_history` DISABLE KEYS */;
INSERT INTO `medical_history` VALUES (1,1,1,'Miguel broke his foot, cast should be worn for a month. Take hydrocodone once a day','1x per day','2025-11-26','2025-12-26',1),(2,1,1,'Testing this thing, no need for a medication','1x a day','2025-11-26',NULL,0),(3,1,1,'once more','bang','2025-11-26',NULL,0);
/*!40000 ALTER TABLE `medical_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medication`
--

DROP TABLE IF EXISTS `medication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medication` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint NOT NULL,
  `patient_id` bigint NOT NULL,
  `drug_name` varchar(255) DEFAULT NULL,
  `dose` varchar(255) DEFAULT NULL,
  `frequency` varchar(255) DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `is_perscription` bit(1) DEFAULT NULL,
  `conflict_details` tinytext,
  `conflict_flag` bit(1) DEFAULT NULL,
  `override_justification` tinytext,
  `route` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `perscription_user_FK` (`doctor_id`),
  KEY `perscription_user_FK_1` (`patient_id`),
  CONSTRAINT `medication_user_FK` FOREIGN KEY (`patient_id`) REFERENCES `user` (`id`),
  CONSTRAINT `medication_user_FK_1` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medication`
--

LOCK TABLES `medication` WRITE;
/*!40000 ALTER TABLE `medication` DISABLE KEYS */;
INSERT INTO `medication` VALUES (1,1,1,'Hydrocodone','200mg','1x day','1 month','take with food',NULL,_binary '',_binary '',NULL,NULL,NULL,'oral'),(2,1,1,'Ibuprofen','200mg','Daily','As long as you need','Take with food',NULL,_binary '',_binary '',NULL,NULL,NULL,'oral');
/*!40000 ALTER TABLE `medication` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL,
  `note_type` tinytext,
  `content` tinytext,
  `timestamp` datetime DEFAULT NULL,
  `attachment_data` mediumblob,
  `attachment_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `note_user_FK` (`patient_id`),
  KEY `note_user_FK_1` (`doctor_id`),
  CONSTRAINT `note_user_FK` FOREIGN KEY (`patient_id`) REFERENCES `user` (`id`),
  CONSTRAINT `note_user_FK_1` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `note`
--

LOCK TABLES `note` WRITE;
/*!40000 ALTER TABLE `note` DISABLE KEYS */;
INSERT INTO `note` VALUES (1,1,1,'TEXT','Miguel came in for a visit today','2025-11-28 03:28:50',NULL,NULL),(4,1,1,'TEXT','Miguel watched stranger things today','2025-11-28 03:47:01',NULL,NULL),(5,1,1,'TEXT','he now has depression','2025-11-28 03:47:10',NULL,NULL),(6,1,1,'TEXT','such a good show','2025-11-28 03:47:23',NULL,NULL),(8,1,1,'TEXT','testing again?','2025-11-28 03:49:12',NULL,NULL),(9,1,1,'TEXT','baaaaaaaaaaaaaaaaaaaaanngggggggggggggggggg','2025-11-28 04:02:29',NULL,NULL),(10,1,1,'TEXT','another lovely test','2025-11-28 04:04:01',NULL,NULL),(11,1,1,'TEXT','testing again','2025-12-02 03:30:48',NULL,NULL);
/*!40000 ALTER TABLE `note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_token`
--

DROP TABLE IF EXISTS `otp_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `used` bit(1) DEFAULT NULL,
  `attempt_count` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `otp_token_user_FK` (`user_id`),
  CONSTRAINT `otp_token_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_token`
--

LOCK TABLES `otp_token` WRITE;
/*!40000 ALTER TABLE `otp_token` DISABLE KEYS */;
INSERT INTO `otp_token` VALUES (1,1,'$2a$10$dKPopDJJkBIn7XuXsRVk/OrKNK8ZHhVciAbz4YIPwo8rjuVos3vbO','2025-11-26 23:16:43','2025-11-26 23:21:43',_binary '',0),(2,1,'$2a$10$IVkwL4Cg2h2hHvgkmRqRjuw3nItF0YKcvXE2RhcBQh84GiqxX4ezq','2025-11-26 23:33:43','2025-11-26 23:38:43',_binary '',0),(3,1,'$2a$10$v4NOUwBEjDRP9cjChW1A/eolWVudfAyGnL6a8ff7QMKmGFBCZqgY.','2025-11-26 23:34:10','2025-11-26 23:39:10',_binary '',0),(4,1,'$2a$10$M6jbLhaFuJJcg/4K72UlTu7B2AAz.L6VFo4b4Wmn4InRZ7Zowj6N6','2025-11-27 21:28:05','2025-11-27 21:33:05',_binary '',0),(5,2,'$2a$10$BFPHbXLMOzAYOjZPaSFHCOMUTla86TGnL6gOTJFW.4onOs1W5xbgq','2025-12-02 18:57:21','2025-12-02 19:02:21',_binary '',0),(6,2,'$2a$10$4uBRFqISiSZpCH/WmJe7eOCZuNwEWRS6mi6pylzR3OI8JAM9eOOgC','2025-12-02 18:57:34','2025-12-02 19:02:34',_binary '',0),(7,2,'$2a$10$Fl7//2Cd/7IBAIJ0it60Ke5dcR2dZXmp9IJJjcIVD2wxQlvLn43Eu','2025-12-02 18:57:36','2025-12-02 19:02:36',_binary '',0),(8,2,'$2a$10$QvpDb8SMVmbNQo48229JY.XrVKdpd5xAOGzZC5osL/vK0RJVYll.q','2025-12-02 19:43:10','2025-12-02 19:48:10',_binary '',0);
/*!40000 ALTER TABLE `otp_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('DOCTOR','NURSE','PATIENT') NOT NULL,
  `date_of_birth` datetime(6) NOT NULL,
  `address` varchar(100) NOT NULL,
  `email` varchar(50) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `gender` varchar(10) NOT NULL,
  `creation_date` datetime NOT NULL,
  `update_date` datetime NOT NULL,
  `failed_login_attempts` int DEFAULT NULL,
  `is_locked` bit(1) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `height` varchar(32) DEFAULT NULL,
  `weight` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_unique` (`user_name`),
  UNIQUE KEY `user_id_IDX` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Miguel','Ocque','miguelocque','$2a$10$jKCkq09fQOTKduchB5SHLuJnMcrDN9/a3cGtaOKwQOBbjISnC2yR.','PATIENT','2004-03-17 05:00:00.000000','454 Pepperell Rd.','miguelmocque@gmail.com','9787273703','MALE','2025-11-26 23:15:25','2025-11-26 23:15:25',0,_binary '\0','2025-11-27 21:28:27.107245','5\'10\"','125 lbs'),(2,'Rajan','Bilan-Cooper','rajanbilancooper','$2a$10$r0ZIN7vtMsEYC/iFnLSHm.F.z2z/GY11GHEnrJ5naIB/lPd3YnS/6','PATIENT','2025-12-01 05:00:00.000000','214 Bay State Rd, Boston, MA','rajan.b.cooper@gmail.com','9787273703','MALE','2025-12-02 18:56:50','2025-12-02 18:56:50',0,_binary '\0','2025-12-02 19:43:27.455021','194cm','80kg'),(3,'Leonadis','K','kthanasi','$2a$10$4GYzgl469DMNA6.BiK1.oe82p7DFdEags5cv8UC8ru8EyV0JCOARy','PATIENT','2000-12-02 05:00:00.000000','214 Bay State Rd, Boston, MA','kthanasi@bu.edu','1234567890','MALE','2025-12-02 19:42:32','2025-12-02 19:42:32',0,_binary '\0','2025-12-02 19:42:32.295554','6ft3','190lbs');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `login_time` datetime DEFAULT NULL,
  `logout_time` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `last_activity_time` datetime DEFAULT NULL,
  `session_token` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKadtubqdpnfx09r9y4jekhilxo` (`session_token`),
  KEY `user_session_user_FK` (`user_id`),
  CONSTRAINT `user_session_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_session`
--

LOCK TABLES `user_session` WRITE;
/*!40000 ALTER TABLE `user_session` DISABLE KEYS */;
INSERT INTO `user_session` VALUES (1,1,NULL,NULL,'2025-11-26 23:17:12','2025-11-27 23:17:12',_binary '','0:0:0:0:0:0:0:1','2025-11-26 23:17:12','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWd1ZWxvY3F1ZSIsInVzZXJJZCI6MSwicm9sZSI6IlBBVElFTlQiLCJpYXQiOjE3NjQxOTkwMzEsImV4cCI6MTc2NDI4NTQzMX0.F3etqYJrSOnWs1Ydk8X_Su_T5ROJsBLPEwrYK0q6UFXLQUwad8xMUnem8im9mfrXrGAVgIqT1ICHyuspnVIjJg'),(2,1,NULL,NULL,'2025-11-26 23:34:27','2025-11-27 23:34:27',_binary '','0:0:0:0:0:0:0:1','2025-11-26 23:34:27','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWd1ZWxvY3F1ZSIsInVzZXJJZCI6MSwicm9sZSI6IlBBVElFTlQiLCJpYXQiOjE3NjQyMDAwNjYsImV4cCI6MTc2NDI4NjQ2Nn0.W7ZqcvkgZX9rvh9ts-dA8gUI3AQOGCjT2J6UbS7doeljFR238Z7IegnUL_4WCVOFfhf4VpeHHJlg1rJa03qLiA'),(3,1,NULL,NULL,'2025-11-27 21:28:27','2025-11-28 21:28:27',_binary '','0:0:0:0:0:0:0:1','2025-11-27 21:28:27','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWd1ZWxvY3F1ZSIsInVzZXJJZCI6MSwicm9sZSI6IlBBVElFTlQiLCJpYXQiOjE3NjQyNzg5MDcsImV4cCI6MTc2NDM2NTMwN30.a7UhpRGSTY2guVZLu71bQPR_OzObTpsprk_VTiZW-4qfkybdbgPtJNBCMc9GTCG4gdPcNioEXwhubhda5v4FFw'),(4,2,NULL,NULL,'2025-12-02 18:57:54','2025-12-03 18:57:54',_binary '','0:0:0:0:0:0:0:1','2025-12-02 18:57:54','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyYWphbmJpbGFuY29vcGVyIiwidXNlcklkIjoyLCJyb2xlIjoiUEFUSUVOVCIsImlhdCI6MTc2NDcwMTg3MywiZXhwIjoxNzY0Nzg4MjczfQ.WHPD4oOYOwn3HLvapFPKA3be-mStZ7-nYuE88pCy2qHv3Yjvq3rjIACm-DlHcOj5imXFY68lENg4bFX_pKTlFA'),(5,2,NULL,NULL,'2025-12-02 19:43:27','2025-12-03 19:43:27',_binary '','0:0:0:0:0:0:0:1','2025-12-02 19:43:27','eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyYWphbmJpbGFuY29vcGVyIiwidXNlcklkIjoyLCJyb2xlIjoiUEFUSUVOVCIsImlhdCI6MTc2NDcwNDYwNywiZXhwIjoxNzY0NzkxMDA3fQ.n5UiI1N8CnKCBcKZdblAI5bf6ATOLMsokBtvTn4k_dAlaMljsdwlsJz3XnTV8F2FjfFmfZ-Wot28nWKoOHRIGA');
/*!40000 ALTER TABLE `user_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'EHR'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-10 16:55:53
