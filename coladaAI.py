import urllib
from bs4 import BeautifulSoup
import copy
import re
import json
import numpy as np
from keras.models import Sequential
from keras.layers import Dense
import ast
from keras.models import load_model
from keras.layers import Dropout

import tensorflow as tf
from tensorflow.python.tools import freeze_graph, optimize_for_inference_lib

from keras import backend as K

def export_model_for_mobile(model_name, input_node_name, output_node_name):
    tf.train.write_graph(K.get_session().graph_def, 'out', \
        model_name + '_graph.pbtxt')

    tf.train.Saver().save(K.get_session(), 'out/' + model_name + '.chkp')

    freeze_graph.freeze_graph('out/' + model_name + '_graph.pbtxt', None, \
        False, 'out/' + model_name + '.chkp', output_node_name, \
        "save/restore_all", "save/Const:0", \
        'out/frozen_' + model_name + '.pb', True, "")

    input_graph_def = tf.GraphDef()
    with tf.gfile.Open('out/frozen_' + model_name + '.pb', "rb") as f:
        input_graph_def.ParseFromString(f.read())

    output_graph_def = optimize_for_inference_lib.optimize_for_inference(
            input_graph_def, [input_node_name], [output_node_name],
            tf.float32.as_datatype_enum)

    with tf.gfile.FastGFile('out/tensorflow_lite_' + model_name + '.pb', "wb") as f:
        f.write(output_graph_def.SerializeToString())

scrapedData = None
with open("scrapedData.json", "r") as f:
    scrapedData = f.read()
scrapedData = json.loads(scrapedData)

quote_page = "https://en.wikipedia.org/wiki/List_of_alcoholic_drinks"
page = urllib.request.urlopen(quote_page)
soup = BeautifulSoup(page, 'html.parser')

def remove_text_inside_brackets(text, brackets="()[]"):
    count = [0] * (len(brackets) // 2) # count open/close brackets
    saved_chars = []
    for character in text:
        for i, b in enumerate(brackets):
            if character == b: # found bracket
                kind, is_close = divmod(i, 2)
                count[kind] += (-1)**is_close # `+1`: open, `-1`: close
                if count[kind] < 0: # unbalanced bracket
                    count[kind] = 0  # keep it
                else:  # found bracket to remove
                    break
        else: # character is not a [balanced] bracket
            if not any(count): # outside brackets
                saved_chars.append(character)
    return ''.join(saved_chars)

#drinks = soup.find_all('a')
#drinkList = []
#for i in drinks:
#    curDrink = i.get_text().strip().lower()
#    curDrink = remove_text_inside_brackets(curDrink)
#    if(len(curDrink)>2 and not "wiki" in curDrink and not "list" in curDrink and not "pedia" in curDrink and not "[" in curDrink):
#        drinkList.append(curDrink)
#drinkList = drinkList[0:364]

drinkList = ['aila',
 'borassus flabellifer',
 'honey',
 'merisa',
 'manx spirit',
 'apfelwein',
 'gouqi jiu',
 'jenever',
 'bagaço',
 'palm wine',
 'brown ale',
 'kasiri',
 'rhum agricole',
 'apples',
 'palm',
 'lozovača',
 'fruit brandy',
 'moonshine',
 'palinka',
 'gin',
 'sake',
 'poitín ',
 'maotai',
 'pastis',
 'table wine',
 'tequila',
 'nihamanchi',
 'tej',
 'sorghum',
 'arak',
 'eau-de-vie',
 'irish whiskey',
 'perry',
 'molasses',
 'guaro',
 'cask ale',
 'canadian whisky',
 'rum',
 'apricots',
 'mbege',
 'chuoi hot',
 'choujiu',
 'poire williams',
 'pulque',
 'sherry',
 'yangmei jiu',
 'sparkling wine',
 'wine',
 'burukutu',
 'grappa',
 'coyol wine',
 'coconut',
 'törkölypálinka',
 'fermenting',
 'baijiu',
 'vinjak',
 'rakı',
 'feni',
 'shōchū',
 'singani',
 'pomegranate',
 'urgwagwa',
 'scotch whisky',
 'zivania',
 'sloe gin',
 'pito',
 'raki',
 'orujo',
 'tonto',
 'marsala',
 'tennessee whiskey',
 'metaxa',
 'witbier',
 'basi',
 'rakia',
 'rice baijiu',
 'plum jerkum',
 'beer',
 'champagne',
 'kirsch',
 'juniper berries',
 'kefir',
 'cauim',
 'fruit beer',
 'viljamovka',
 'pale ale',
 'corn beer',
 'korn',
 'ginger wine',
 'calvados',
 'cider',
 'plum wine',
 'țuică',
 'raicilla',
 'kilju',
 'wheat beer',
 'port',
 'brem',
 'umeshu',
 'tsipouro',
 'pinga',
 'mamajuana',
 'horilka',
 'honey-flavored liqueur',
 'ti root',
 'sweet potato',
 'majmunovača',
 'myrica rubra',
 'madeira',
 'damassine',
 'ale',
 'blaand',
 'sahti',
 'cognac',
 'tepache',
 'tembo',
 'wheat',
 'whiskey',
 'cocoroco',
 'slivovitz',
 'pisco',
 'dunjevača',
 'vermouth',
 'bitter ale',
 'marc',
 'awamori',
 'stout',
 'distillation',
 'ginger ale',
 'buckwheat',
 'old ale',
 'ouzo',
 'plums',
 'sugar',
 'tuak',
 'rye',
 'tescovină',
 'krushova rakia',
 'huangjiu',
 'tsikoudia',
 'desi daru',
 'icariine liquor',
 'edit',
 'ruou gao',
 'akvavit',
 'borovička',
 'neutral grain spirit',
 'sangria',
 'saliva-fermented beverages',
 'tiswin',
 'barley wine',
 'fortified wine',
 'shochu',
 'japanese whisky',
 'democratic republic of the congo',
 'applejack',
 'stock ale',
 'pálinka',
 'kvass',
 'damson gin',
 'grapes',
 'milk',
 'pilsener',
 'tongba',
 'sonti',
 'boza',
 'kajsijevača',
 'bourbon whiskey',
 'armagnac',
 'fruit wine',
 'arenga pinnata',
 'mild ale',
 'chicha',
 'chungju',
 'pears',
 'himbeergeist',
 'williamine',
 'rye beer',
 'kaoliang wine',
 'gouqi',
 'ginger beer',
 'sugarcane',
 'clairin',
 'pineapples',
 'bandundu province',
 'hypopta agavis',
 'shōchū ',
 'parakari',
 'lager',
 'tesguino',
 'poiré',
 'makgeolli',
 'tiquira',
 'mead',
 'obstwasser',
 'porter',
 'pomace',
 'agave',
 'french caribbean',
 'kumis',
 'mezcal',
 'rye whiskey',
 'thwon',
 'arrack',
 'trester',
 'absinthe',
 'poitín',
 'bilibili',
 'aguardiente',
 'betsa-betsa',
 'scotch ale',
 'quinces',
 'vinsanto',
 'puncheon rum',
 'maerzen/oktoberfest beer',
 'ţuică',
 'sambuca',
 'jabukovača',
 'cassava',
 'whisky',
 'kasikisi',
 'okolehao',
 'bock',
 'millet',
 'schwarzbier',
 'millet beer',
 'pomace wine',
 'absinthe spoon',
 'ogogoro',
 'kaisieva rakia',
 'cashew',
 'pale lager',
 'brandy',
 'soju',
 'slivova rakia',
 'vodka',
 'toddy',
 'barleywine',
 'cachaça',
 'schnapps',
 'šljivovica',
 'beers']

X = []
y = []
# one hot encoding
for i in scrapedData:
    curRating = i['Rating']
    if curRating == 0.0:
        continue
    curDrink = np.zeros(len(drinkList))
    for j in range(len(drinkList)):
        for k in [x.lower() for x in i['Ingred']]:
            if(drinkList[j] in k):
                curDrink[j] = 1
    X.append(curDrink)
    y.append(curRating)

model = Sequential()
model.add(Dense(50, input_dim=len(drinkList), activation='relu', kernel_initializer='lecun_normal'))
# model.add(Dense(50, activation='relu', kernel_initializer='lecun_normal'))
# model.add(Dense(50, activation='relu', kernel_initializer='lecun_normal'))
# model.add(Dropout(0.5))
# model.add(Dense(50, activation='relu', kernel_initializer='lecun_normal'))
# model.add(Dense(50, activation='relu', kernel_initializer='lecun_normal'))
model.add(Dense(1, activation='relu', kernel_initializer='lecun_normal'))
model.summary()

model.compile(loss='mean_squared_error', optimizer='adam', metrics=['accuracy'])
model.fit(np.array(X), np.array(y), epochs=50, batch_size=5,  verbose=1, validation_split=0.2)

model.save('coladaAIModel.h5')
export_model_for_mobile('coladaAIModel', 'dense_1_input', "dense_2/Relu")