#!/usr/bin/env python
# encoding: utf-8

import tweepy #https://github.com/tweepy/tweepy
import json
import os
import sys
import shutil

import dateutil.parser
from datetime import datetime
import requests

#Twitter API credentials
consumer_key = "xxxxxxxxxx"
consumer_secret = "xxxxxxxxxx"
access_key = "xxxxxxxxxx"
access_secret = "xxxxxxxxxx"

MAX_ALLOWED_COUNT = 199

# ref: https://github.com/morinokami/twitter-image-downloader/blob/master/twt_img/twt_img.py
def get_all_tweets(screen_name):
	# Twitter only allows access to a users most recent 3240 tweets with this method
	
	# authorize twitter, initialize tweepy
	auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
	auth.set_access_token(access_key, access_secret)
	api = tweepy.API(auth)
	
	# initialize a list to hold all the tweepy Tweets
	alltweets = []	
	
	# make initial request for most recent tweets (200 is the maximum allowed count)
	new_tweets = api.user_timeline(screen_name=screen_name, count=MAX_ALLOWED_COUNT, tweet_mode="extended")
	
	# save most recent tweets
	alltweets.extend(new_tweets)
	
	# save the id of the oldest tweet less one
	oldest = alltweets[-1].id - 1

	# keep grabbing tweets until there are no tweets left to grab
	while len(new_tweets) > 0:
		
		#all subsiquent requests use the max_id param to prevent duplicates
		new_tweets = api.user_timeline(screen_name=screen_name, count=MAX_ALLOWED_COUNT, max_id=oldest)
		
		#save most recent tweets
		alltweets.extend(new_tweets)
		
		#update the id of the oldest tweet less one
		oldest = alltweets[-1].id - 1

	print("Total tweets downloaded from %s are %s" % (screen_name, len(alltweets)))
	
	return alltweets

def extract_media_list(tweet):
	"""Return a list of url(s) which represents the image(s) embedded in tweet.
	Args:
		tweet: A dict object representing a tweet.
	"""

	entities = tweet._json["entities"]
	if "media" not in entities:
		return None

	entities = tweet._json["extended_entities"]
	if "media" in entities:
		urls = []		
		for x in entities["media"]:
			if x["type"] == "photo":
				urls.append(x["media_url"])
			elif x["type"] in ["video", "animated_gif"]:
				variants = x["video_info"]["variants"]
				variants.sort(key=lambda x: x.get("bitrate", 0))
				url = variants[-1]["url"].rsplit("?tag")[0]
				urls.append(url)

		# urls = [x["media_url"] for x in entities["media"]]
		# extended_entities = tweet._json["extended_entities"]
		# if extended_entities:
			# extra = [x["media_url"] for x in extended_entities["media"]]
			# urls = set(urls + extra)
		return urls
	else:
		return None

def save_media(image, path, timestamp, size="large"):
	"""Download and save image to path.
	Args:
		image: The url of the image.
		path: The directory where the image will be saved.
		timestamp: The time that the image was uploaded.
			It is used for naming the image.
		size: Which size of images to download.
	"""

    # def print_status(s):
    #     import sys

    #     sys.stdout.write("\u001b[1K")
    #     spinner = ["-", "\\", "|", "/"][self.count % 4]
    #     print(f"\r{spinner} {s}", end="")

	if not image:
		print('invalid media file')

    # image's path with a new name
	ext = os.path.splitext(image)[1]
	name = timestamp + ext
	save_dest = os.path.join(path, name)
	filetype = 'image' if ext not in [".mp4"] else 'video'

    # save the image in the specified directory if
	if (os.path.exists(save_dest)):
        # print_status(f"Skipping {name}: already downloaded")
		print('Skipping ', name, ' already downloaded')
	else:
		if ext not in [".mp4"]:
			r = requests.get(image + (size and ":" or "") + size, stream=True)
		else:
			r = requests.get(image, stream=True)

		if r.status_code == 200:
			with open(save_dest, "wb") as f:
				r.raw.decode_content = True
				shutil.copyfileobj(r.raw, f)
			# self.count += 1
			print(name, ' saved')
	return filetype, name		

def fetch_tweets(screen_names):

	# initialize the list to hold all tweets from all users
	alltweets=[]

	# get all tweets for each screen name
	for  screen_name in screen_names:
		alltweets.extend(get_all_tweets(screen_name))

	return alltweets

def store_tweets(alltweets, user_id, file='tweets.json'):

	# a list of all formatted tweets
	tweet_list=[]

	# print(alltweets[0])
	# print(user_id)

	if not os.path.exists(user_id):
		os.makedirs(user_id)

	for tweet in alltweets:

		# print(tweet)
        # create a file name using the timestamp of the image
		# ts = dateutil.parser.parse(tweet.created_at.timestamp()
		# ts = int(ts)
        # value = datetime.fromtimestamp(timestamp)
		# fname = datetime.fromtimestamp(ts).strftime("%Y-%m-%d-%H-%M-%S")
		# fname = tweet.created_at.strftime("%Y-%m-%d-%H-%M-%S")
		fname = tweet.id_str

		saved_file_list = []
        # save the image
		meida_list = extract_media_list(tweet)
		if meida_list:
			counter = 0
			for meida in meida_list:
				filetype, saved_name = save_media(meida, user_id, fname + "_" + str(counter))
				saved_file_list.append({'url': saved_name, 'filetype': filetype, 'filename': saved_name})
				counter += 1


			# a dict to contain information about single tweet
			tweet_information=dict()

			# RT -> tweet.retweeted_status.full_text
			# text of tweet
			tweet_information['text']=tweet.full_text if hasattr(tweet, 'full_text') else tweet.text

			# date and time at which tweet was created
			tweet_information['created_at']=tweet.created_at.strftime("%Y-%m-%d %H:%M:%S")

			# id of this tweet
			tweet_information['id']=tweet.id_str

			tweet_information['files']=saved_file_list

			# # retweet count
			# tweet_information['retweet_count']=tweet.retweet_count

			# # favourites count
			# tweet_information['favorite_count']=tweet.favorite_count

			# # screename of the user to which it was replied (is Nullable)
			# tweet_information['in_reply_to_screen_name']=tweet.in_reply_to_screen_name

			# user information in user dictionery
			user_dictionery=tweet._json['user']

			print(user_dictionery)

			# # no of followers of the user
			# tweet_information['followers_count']=user_dictionery['followers_count']

			# screename of the person who tweeted this
			tweet_information['screen_name']=user_dictionery['screen_name']

			created_at = dateutil.parser.parse(user_dictionery['created_at'])

			_, saved_name = save_media(user_dictionery['profile_image_url'], user_id, 'avatar', size='')


			tweet_information['author']={'id': user_dictionery['id_str'], 'username': user_dictionery['screen_name'], 'displayName': user_dictionery['name'], 'image': saved_name, 'created_at': created_at.strftime("%Y-%m-%d %H:%M:%S")}


			# add this tweet to the tweet_list
			tweet_list.append(tweet_information)

	save_dest = os.path.join(user_id, file)
	# open file desc to output file with write permissions
	with open(save_dest,'w', encoding='utf-8') as f:
		# dump tweets to the file
		json.dump(tweet_list, f, indent=4, sort_keys=True)


if __name__ == '__main__':
	
	# pass in the username of the account you want to download
	alltweets=get_all_tweets(sys.argv[1])

	# store the data into json file
	if len(sys.argv) > 2 and len(sys.argv[2])>0:
		store_tweets(alltweets, sys.argv[1], sys.argv[2])
	else :
		store_tweets(alltweets, sys.argv[1])