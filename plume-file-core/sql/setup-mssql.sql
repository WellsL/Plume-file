CREATE TABLE PLM_FILE (
  ID BIGINT PRIMARY KEY,
  FILENAME VARCHAR(255) NULL,
  FILE_TYPE VARCHAR(255) NOT NULL,
);

CREATE TABLE PLM_FILE_DATA (
  ID BIGINT PRIMARY KEY,
  ID_FILE BIGINT,
  DATA VARBINARY(MAX) NOT NULL,
  CONSTRAINT PLM_FILE_FK FOREIGN KEY (ID_FILE)
  REFERENCES PLM_FILE(ID)
);

GO