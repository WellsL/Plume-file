DROP TABLE IF EXISTS `PLM_FILE_DATA`;

CREATE TABLE `PLM_FILE_DATA`
(
    `unique_name` VARCHAR(255) NOT NULL,
    `data`        MEDIUMBLOB   NOT NULL,
    FOREIGN KEY (unique_name) REFERENCES PLM_FILE (unique_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
