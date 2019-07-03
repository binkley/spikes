CREATE TABLE IF NOT EXISTS LOCATIONS
(
    ID     INT AUTO_INCREMENT PRIMARY KEY,
    "NAME" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS CHEFS
(
    ID     INT AUTO_INCREMENT PRIMARY KEY,
    "NAME" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS RECIPES
(
    ID      INT AUTO_INCREMENT PRIMARY KEY,
    "NAME"  TEXT NOT NULL,
    CHEF_ID INT  NOT NULL,
    CONSTRAINT FK_RECIPES_CHEF_ID_ID FOREIGN KEY (CHEF_ID) REFERENCES CHEFS (ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS INGREDIENTS
(
    ID        INT AUTO_INCREMENT PRIMARY KEY,
    "NAME"    TEXT NOT NULL,
    CHEF_ID   INT  NOT NULL,
    RECIPE_ID INT  NULL,
    CONSTRAINT FK_INGREDIENTS_CHEF_ID_ID FOREIGN KEY (CHEF_ID) REFERENCES CHEFS (ID) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT FK_INGREDIENTS_RECIPE_ID_ID FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);
