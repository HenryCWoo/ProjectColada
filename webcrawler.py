import urllib
from bs4 import BeautifulSoup
import copy
import re
import json

quote_page = "https://www.liquor.com/recipes/"
page = urllib.request.urlopen(quote_page)
soup = BeautifulSoup(page, 'html.parser')

# get link to all pages that contain recipes
allPages = [] # excludes the first page

for link in soup.find_all('a'):
    curLink = link.get('href')
    if('www.liquor.com/recipes/page/' in curLink):
        allPages.append(curLink)
        
maxPages = 0 # number of pages to visit

for i in allPages:
    sampleLink = i.split('/')
    curNumber = int(sampleLink[-2])
    if(curNumber > maxPages):
        maxPages = curNumber

sampleLink = allPages[0].split('/')
for i in range(2, maxPages):
    curLink = copy.deepcopy(sampleLink)
    curLink[-2] = str(i)
    curLink = "/".join(curLink)
    allPages.append(curLink)
    
# get links to the recipe pages
recipeLinks = set()

# get recipe links from first page
for link in soup.find_all('a'):
        curLink = link.get('href')
        if('www.liquor.com/recipes/' in curLink and not '/page/' in curLink):
            recipeLinks.add(curLink)

# get recipe links from the rest of the pages
for pageLink in allPages:
    page = urllib.request.urlopen(pageLink)
    soup = BeautifulSoup(page, 'html.parser')
    for link in soup.find_all('a'):
        curLink = link.get('href')
        if('www.liquor.com/recipes/' in curLink and not '/page/' in curLink):
            recipeLinks.add(curLink)

def getAbout(soup):
    about = soup.find("span", {'itemprop': "description"})
    if(about == None):
        return None
    about = about.get_text()
    about = about.strip()
    return about
     
def getIngredients(soup):
    ing = soup.find_all("div", {'class': "row x-recipe-unit"})
    ingredients = []
    for i in ing:
        curText = i.get_text()
        endOfIng = curText.find("\t")
        rawString = curText[0:endOfIng]
        ingredients.append(rawString)
    for i in range(len(ingredients)):
        ingredients[i] = " ".join(ingredients[i].split())
        ingredients[i] = ingredients[i].replace(" 1⁄2", ".5")
        ingredients[i] = ingredients[i].replace("1⁄2", ".5")
        ingredients[i] = ingredients[i].replace("*", "")
    return ingredients

def getGarnish(soup):
    garn = soup.find_all("span", {"class": "parts-value"})
    garnishes = []
    for i in garn:
        curText = i.get_text().strip()
        garnishes.append(curText.replace("*", "").replace("\xa0", " "))
    return garnishes

def getGlass(soup):
    glass = soup.find("div", {"class": "col-xs-9 recipe-link x-recipe-glasstype no-padding"})
    if(glass == None):
        return None
    return glass.get_text().replace("*", "").strip()

def getInstructions(soup):
    instructions = soup.find("div", {"itemprop": "recipeInstructions"})
    if(instructions == None):
        return None
    instructions = instructions.find_all("p")
    for i in range(len(instructions)):
        instructions[i] = instructions[i].get_text().strip()
    instructions = "\n\n".join(instructions)
    return instructions

# extract information that can only be one attribute
def findProfileAttrSingles(soup, className):
    curText = soup.find("div", {"class": className})
    if(curText != None):
        curText = curText.get_text().replace("*", "").strip()
    return curText

classNamesSingles = { 
              "Cocktail Type":"col-xs-7 x-recipe-type",
              "Served":"col-xs-7 x-recipe-served",
              "Prep":"col-xs-7 x-recipe-preparation",
              "Strength":"col-xs-7 x-recipe-strength", 
              "Difficulty":"col-xs-7 x-recipe-difficulty",
              "Theme":"col-xs-7 x-recipe-themes"}

def getProfileSingles(soup):
    thisProfile = {}
    for i in classNamesSingles.keys():
        thisProfile[i] = findProfileAttrSingles(soup, classNamesSingles[i])
    return thisProfile

# extract information that can have multiple attributes
def findProfileAttrMult(soup, className):
    curText = soup.find("div", {"class": className})
    if(curText == None):
        return None
    curText = curText.find_all("a")
    if(len(curText) == 0):
        return None
    results = []
    for i in curText:
        curLine = i.get_text().replace("*", "").strip()
        results.append(curLine)
    return results

classNamesMult = {"Flavors":"col-xs-12 text-center x-recipe-flavor recipe-link",
                  "Hours":"col-xs-7 x-recipe-hours",
                  "Brands": "col-xs-7 x-recipe-brands",
                  "Occasions": "col-xs-7 x-recipe-occasions",
                  "Base Spirit":"col-xs-7 x-recipe-spirit"}

def getProfileMult(soup):
    thisProfile = {}
    for i in classNamesMult.keys():
        thisProfile[i] = findProfileAttrMult(soup, classNamesMult[i])
    return thisProfile

# get the remaining profile attributes

def getDrinkName(soup):
    try:    
        return soup.find("h1", {"itemprop": "name"}).get_text()
    except:
        return None
    
def getAuthor(soup):
    try:
        return soup.find("a", {"rel": "author"}).get_text()
    except:
        return None

# use Google to crowdsource ratings
# returns a tuple -> (collective rating, collective votes)

def removeNonAscii(s):
    stripped = (c for c in s if 0 < ord(c) < 127)
    return ''.join(stripped)

def getGoogleRating(drinkName):
    try:
        alphaNumericName = removeNonAscii(drinkName)
        url = "https://www.google.com/search?q=" + "+".join(alphaNumericName.split(" ")) + "+cocktail+rating"
        headers = {'User-Agent':'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0'}  
        req = urllib.request.Request(url, headers = headers)
        page = urllib.request.urlopen(req)
        soup = BeautifulSoup(page, 'html.parser')
        
        ratingsList = []
        rawRatingsList = soup.find_all("div", {"class": "slp f"})
        for item in rawRatingsList:
            curText = " ".join(item.get_text().split())
            if("Rating" in curText):
                curText = curText.replace("\u200e", "")
                ratingsList.append(curText)
        
        curRating = 0.0
        curVotes = 0.0
        for i in ratingsList:
            curItem = i.split(" ")
            curItemRating = [0.0,0]
            for word in range(len(curItem)):
                if("Rating" in curItem[word] and len(curItem) > 2):
                    try:
                        curItemRating[0] = float(curItem[word+1].replace(",", ""))
                    except:
                        break
                if(("vote" in curItem[word] or "review" in curItem[word]) and word>1):
                    curItemRating[1] = int(curItem[word-1].replace(",", ""))
                if((curVotes+curItemRating[1])!= 0):
                    curRating = (curRating*curVotes+curItemRating[0]*curItemRating[1])/(curVotes+curItemRating[1])
                    curVotes = (curVotes+curItemRating[1])
        return((curRating, int(curVotes)))
    except:
        return((0,0))
    
    

drinksList = []
for i in recipeLinks:
    print(i)
    try:
        page = urllib.request.urlopen(i)
        soup = BeautifulSoup(page, 'html.parser')
    except:
        continue
    
    mDict = {}
    mDict["Drink Name"] = getDrinkName(soup)
    mDict["Author"] = getAuthor(soup)
    mDict["About"] = getAbout(soup)
    mDict["Ingred"] = getIngredients(soup)
    mDict["Garnish"] = getGarnish(soup)
    mDict["Glass"] = getGlass(soup)
    mDict["Instructions"] = getInstructions(soup)
    
    singleAttrs = getProfileSingles(soup)
    for keys in singleAttrs.keys():
        mDict[keys] = singleAttrs[keys]
    
    multAttrs = getProfileMult(soup)
    for keys in multAttrs.keys():
        mDict[keys] = multAttrs[keys]
    
    googleRating = (0,0)
    try:
         googleRating = getGoogleRating(getDrinkName(soup))
    except:
        print(getDrinkName(soup))
    mDict["Rating"] = googleRating[0]
    mDict["Rating Count"] = googleRating[1]
    drinksList.append(mDict)

#save data to json file
with open("scrapedData.json", "w") as fout:
    json.dump(drinksList, fout)
    

