IF COL_LENGTH('[User]', 'fullName') IS NULL
    EXEC('ALTER TABLE [User] ADD fullName NVARCHAR(255) NULL');

IF COL_LENGTH('Customer', 'createBy') IS NULL
    EXEC('ALTER TABLE Customer ADD createBy NVARCHAR(100) NULL');
