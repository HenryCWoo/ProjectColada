#!/usr/bin/python

import MySQLdb
import json
import unicodedata

def addQuery(sqlPrefix, sqlSuffix, sqlKey, key, curDict):
    curItem = curDict[key]
    if(curItem != None):
        sqlPrefix += "`" + sqlKey + "`,"
        sqlSuffix += "'" + str(str(curItem).encode('ascii','ignore').decode("utf-8")) + "',"
    else:
        sqlPrefix += "`" + sqlKey + "`,"
        sqlSuffix += "NULL,"
    return sqlPrefix, sqlSuffix

def removeDupe(seq):
    seen = set()
    seen_add = seen.add
    return [x for x in seq if not (x in seen or seen_add(x))]

# Open database connection
#PROVIDE CREDENTIALS HERE
db = MySQLdb.connect(host=,
                      user=,
                      password=,
                      db=)

dictKeys = ["Drink Name", "Author", "About", "Instructions", "Rating", 
            "Rating Count", "Prep", "Strength", "Difficulty", "Theme",
            "Glass", "Cocktail Type", "Served"]
sqlKeys = ["DrinkName", "Author", "About", "Instructions", "Rating", 
           "RatingCount", "Preparation", "Strength", "Difficulty", "Theme",
           "Glass", "CocktailType", "Served"]

scrapedData = None
with open("scrapedData.json", "r") as f:
    scrapedData = f.read()
scrapedData = json.loads(scrapedData)

# prepare a cursor object using cursor() method
cursor = db.cursor()

for i in scrapedData:
    # Prepare SQL query to INSERT a record into the database.
    sqlPrefix = "INSERT INTO `Drinks` ("
    sqlSuffix = "VALUES ("
    
    for dictKey, sqlKey in zip(dictKeys, sqlKeys): 
        curItem = addQuery(sqlPrefix, sqlSuffix, sqlKey, dictKey, i)
        sqlPrefix = curItem[0]
        sqlSuffix = curItem[1]

    if(sqlPrefix[-1] == ","):
        sqlPrefix = sqlPrefix[:-1]+") "
    if(sqlSuffix[-1] == ","):
        sqlSuffix = sqlSuffix[:-1]+")"

    sql = sqlPrefix + sqlSuffix
#    print(sql)
    try:
        # Execute the SQL command
        cursor.execute(sql)
        # Commit your changes in the database
        db.commit()
    except:
        # Rollback in case there is any error
        db.rollback()

    # populate ingredients relation
    for ing in i["Ingred"]:
        ingSql = "INSERT INTO `Contains_Ingred` (`Drink_Name`, `Ingredient_Name`, `Amount`) VALUES ('" + i["Drink Name"] + "', "
        curItem = removeDupe(ing.replace(" oz", "").split(" "))
        part = -1
        if(curItem[0].isnumeric() or "⁄" in curItem[0] or "." in curItem[0]):
            part = curItem[0]
            del curItem[0]
        ingSql += "'" + " ".join(curItem) + "', "
       
        if(part != -1):
            ingSql += "'" + part + "')"
        else:
            ingSql += "NULL)"
        try:
            # Execute the SQL command
            cursor.execute(ingSql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()
    
    if(i["Base Spirit"]):
        for item in i["Base Spirit"]:
            sql = "INSERT INTO `Base_Spirit` (`DrinkName`, `BaseSpirit`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
    
    if(i["Brands"]):
        for item in i["Brands"]:
            sql = "INSERT INTO `Brands` (`DrinkName`, `Brand`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
    
    if(i["Flavors"]):
        for item in i["Flavors"]:
            sql = "INSERT INTO `Flavors` (`DrinkName`, `Flavor`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
    
    if(i["Garnish"]):
        for item in i["Garnish"]:
            sql = "INSERT INTO `Garnish` (`DrinkName`, `Garnish`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
    
    if(i["Occasions"]):
        for item in i["Occasions"]:
            sql = "INSERT INTO `Occasions` (`DrinkName`, `Occasion`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
    
    if(i["Hours"]):
        for item in i["Hours"]:
            sql = "INSERT INTO `Hours` (`DrinkName`, `TOD`) VALUES ('" + i["Drink Name"] + "', '" + item + "')"
            print(sql)
            try:
                # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
            except:
                # Rollback in case there is any error
                db.rollback()
       
       

# disconnect from server
db.close()
