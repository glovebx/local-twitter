package xyz.mirage.app.datasource.network

import xyz.mirage.app.business.domain.models.Attachment
import xyz.mirage.app.business.domain.models.Post
import xyz.mirage.app.business.domain.models.Profile

const val unauthorizedError = "{\n" +
        "    \"error\": {\n" +
        "        \"type\": \"AUTHORIZATION\",\n" +
        "        \"message\": \"you are not the owner\"\n" +
        "    }\n" +
        "}"

const val notFoundResponse = "{\n" +
        "    \"error\": {\n" +
        "        \"type\": \"NOT_FOUND\",\n" +
        "        \"message\": \"resource: post with value: 141167453247688294 not found\"\n" +
        "    }\n" +
        "}"

val mockAuthor = Profile(
    id = "1410270110072967168",
    username = "sentrionic",
    displayName = "Sense",
    image = "https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon",
    bio = null,
    banner = null,
    followee = 0,
    followers = 0,
    following = false,
    createdAt = "2021-07-04T14:35:56.340455Z"
)

val mockFile = Attachment(
    url = "https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqjpgdqu00000410bnehpu3o.jpg",
    filename = "ckqjpgdqu00000410bnehpu3o.jpg",
    filetype = "image/jpeg"
)

val mockPost = Post(
    id = "1410277092372779008",
    text = "Check out this cool pic #wallpaper",
    likes = 0,
    liked = false,
    retweets = 0,
    retweeted = false,
    isRetweet = false,
    createdAt = "2021-07-04T14:45:15.029964Z",
    file = mockFile,
    profile = mockAuthor,
)

/**
 * 20 posts from api
 */
const val postListResponse: String = "{\n" +
        "    \"hasMore\": true,\n" +
        "    \"posts\": [\n" +
        "        {\n" +
        "            \"id\": \"1411697618697850880\",\n" +
        "            \"text\": \"Look at this cool image #wallpaper\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": true,\n" +
        "            \"file\": {\n" +
        "                \"url\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqpb3gqw00000446l2w0vzsg.jpg\",\n" +
        "                \"filetype\": \"image/jpeg\",\n" +
        "                \"filename\": \"ckqpb3gqw00000446l2w0vzsg.jpg\"\n" +
        "            },\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411695275386343424\",\n" +
        "                \"username\": \"sentrionic\",\n" +
        "                \"displayName\": \"Sense\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "                \"banner\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/header_photo/1411695275386343424/1412695647101915136.jpeg\",\n" +
        "                \"bio\": \"The owner of this site\",\n" +
        "                \"followers\": 0,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": false,\n" +
        "                \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T14:45:15.029964Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411697519418675200\",\n" +
        "            \"text\": \"Hello World\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": true,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411695275386343424\",\n" +
        "                \"username\": \"sentrionic\",\n" +
        "                \"displayName\": \"Sense\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "                \"banner\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/header_photo/1411695275386343424/1412695647101915136.jpeg\",\n" +
        "                \"bio\": \"The owner of this site\",\n" +
        "                \"followers\": 0,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": false,\n" +
        "                \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T14:44:51.360036Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674532476882944\",\n" +
        "            \"text\": \"Profit-focused client-server access\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:30.84561Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674529607979008\",\n" +
        "            \"text\": \"Balanced upward-trending encryption\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:30.164497Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674526692937728\",\n" +
        "            \"text\": \"Switchable zerotolerance circuit\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:29.466357Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674523824033792\",\n" +
        "            \"text\": \"Networked contextually-based initiative\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:28.782944Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674520934158336\",\n" +
        "            \"text\": \"Expanded user-facing focusgroup\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:28.097827Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674518040088576\",\n" +
        "            \"text\": \"Innovative context-sensitive help-desk\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:27.404005Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674515133435904\",\n" +
        "            \"text\": \"Synergized bottom-line throughput\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:26.712893Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674512189034496\",\n" +
        "            \"text\": \"Reduced client-driven knowledgebase\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:26.011929Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674509064278016\",\n" +
        "            \"text\": \"Facetoface 5thgeneration utilisation\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:25.269307Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674506119876608\",\n" +
        "            \"text\": \"Proactive fresh-thinking architecture\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:24.565367Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674503263555584\",\n" +
        "            \"text\": \"Programmable object-oriented customerloyalty\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:23.88039Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674471051300864\",\n" +
        "            \"text\": \"Total 3rdgeneration capability\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:16.20059Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674468194979840\",\n" +
        "            \"text\": \"Vision-oriented dedicated forecast\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:15.520115Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674465317687296\",\n" +
        "            \"text\": \"Decentralized executive interface\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:14.83385Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674462507503616\",\n" +
        "            \"text\": \"Seamless bi-directional intranet\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:14.163726Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674459626016768\",\n" +
        "            \"text\": \"Team-oriented full-range circuit\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:13.477131Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674456757112832\",\n" +
        "            \"text\": \"Vision-oriented grid-enabled leverage\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:12.792493Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674453925957632\",\n" +
        "            \"text\": \"Versatile dedicated benchmark\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674444610408448\",\n" +
        "                \"username\": \"rMYXnFtzVmb\",\n" +
        "                \"displayName\": \"Mr. Ken Cronin\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:09.89687Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:12.118108Z\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

const val postWithId: String = "{\n" +
        "    \"id\":\"1410277092372779008\",\n" +
        "    \"text\":\"Check out this cool pic #wallpaper\",\n" +
        "    \"likes\":0,\n" +
        "    \"liked\":false,\n" +
        "    \"retweets\":0,\n" +
        "    \"retweeted\":false,\n" +
        "    \"isRetweet\":false,\n" +
        "    \"file\":{\n" +
        "        \"url\":\"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqjpgdqu00000410bnehpu3o.jpg\",\n" +
        "        \"filetype\":\"image/jpeg\",\n" +
        "        \"filename\":\"ckqjpgdqu00000410bnehpu3o.jpg\"\n" +
        "    },\n" +
        "    \"author\":{\n" +
        "        \"id\":\"1410270110072967168\",\n" +
        "        \"username\":\"sentrionic\",\n" +
        "        \"displayName\":\"Sense\",\n" +
        "        \"image\":\"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "        \"banner\":null,\n" +
        "        \"bio\":null,\n" +
        "        \"followers\":0,\n" +
        "        \"followee\":0,\n" +
        "        \"following\":false,\n" +
        "        \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "    },\n" +
        "    \"createdAt\":\"2021-07-04T14:45:15.029964Z\"\n" +
        "}"

const val postLiked: String = "{\n" +
        "    \"id\":\"1410277092372779008\",\n" +
        "    \"text\":\"Check out this cool pic #wallpaper\",\n" +
        "    \"likes\":1,\n" +
        "    \"liked\":true,\n" +
        "    \"retweets\":0,\n" +
        "    \"retweeted\":false,\n" +
        "    \"isRetweet\":false,\n" +
        "    \"file\":{\n" +
        "        \"url\":\"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqjpgdqu00000410bnehpu3o.jpg\",\n" +
        "        \"filetype\":\"image/jpeg\",\n" +
        "        \"filename\":\"ckqjpgdqu00000410bnehpu3o.jpg\"\n" +
        "    },\n" +
        "    \"author\":{\n" +
        "        \"id\":\"1410270110072967168\",\n" +
        "        \"username\":\"sentrionic\",\n" +
        "        \"displayName\":\"Sense\",\n" +
        "        \"image\":\"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "        \"banner\":null,\n" +
        "        \"bio\":null,\n" +
        "        \"followers\":0,\n" +
        "        \"followee\":0,\n" +
        "        \"following\":false,\n" +
        "        \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "    },\n" +
        "    \"createdAt\":\"2021-07-04T14:45:15.029964Z\"\n" +
        "}"

const val postRetweeted: String = "{\n" +
        "    \"id\":\"1410277092372779008\",\n" +
        "    \"text\":\"Check out this cool pic #wallpaper\",\n" +
        "    \"likes\":0,\n" +
        "    \"liked\":false,\n" +
        "    \"retweets\":1,\n" +
        "    \"retweeted\":true,\n" +
        "    \"isRetweet\":false,\n" +
        "    \"file\":{\n" +
        "        \"url\":\"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqjpgdqu00000410bnehpu3o.jpg\",\n" +
        "        \"filetype\":\"image/jpeg\",\n" +
        "        \"filename\":\"ckqjpgdqu00000410bnehpu3o.jpg\"\n" +
        "    },\n" +
        "    \"author\":{\n" +
        "        \"id\":\"1410270110072967168\",\n" +
        "        \"username\":\"sentrionic\",\n" +
        "        \"displayName\":\"Sense\",\n" +
        "        \"image\":\"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "        \"banner\":null,\n" +
        "        \"bio\":null,\n" +
        "        \"followers\":0,\n" +
        "        \"followee\":0,\n" +
        "        \"following\":false,\n" +
        "        \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "    },\n" +
        "    \"createdAt\":\"2021-07-04T14:45:15.029964Z\"\n" +
        "}"

const val shortListResponse: String = "{\n" +
        "    \"hasMore\": false,\n" +
        "    \"posts\": [\n" +
        "        {\n" +
        "            \"id\": \"1411697618697850880\",\n" +
        "            \"text\": \"Look at this cool image #wallpaper\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": true,\n" +
        "            \"file\": {\n" +
        "                \"url\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/media/ckqpb3gqw00000446l2w0vzsg.jpg\",\n" +
        "                \"filetype\": \"image/jpeg\",\n" +
        "                \"filename\": \"ckqpb3gqw00000446l2w0vzsg.jpg\"\n" +
        "            },\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411695275386343424\",\n" +
        "                \"username\": \"sentrionic\",\n" +
        "                \"displayName\": \"Sense\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "                \"banner\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/header_photo/1411695275386343424/1412695647101915136.jpeg\",\n" +
        "                \"bio\": \"The owner of this site\",\n" +
        "                \"followers\": 0,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": false,\n" +
        "                \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T14:45:15.029964Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411697519418675200\",\n" +
        "            \"text\": \"Hello World\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": true,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411695275386343424\",\n" +
        "                \"username\": \"sentrionic\",\n" +
        "                \"displayName\": \"Sense\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "                \"banner\": \"https://harmony-cdn.s3.eu-central-1.amazonaws.com/files/header_photo/1411695275386343424/1412695647101915136.jpeg\",\n" +
        "                \"bio\": \"The owner of this site\",\n" +
        "                \"followers\": 0,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": false,\n" +
        "                \"createdAt\": \"2021-07-04T14:35:56.340455Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T14:44:51.360036Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674532476882944\",\n" +
        "            \"text\": \"Profit-focused client-server access\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:30.84561Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674529607979008\",\n" +
        "            \"text\": \"Balanced upward-trending encryption\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:30.164497Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674526692937728\",\n" +
        "            \"text\": \"Switchable zerotolerance circuit\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:29.466357Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674523824033792\",\n" +
        "            \"text\": \"Networked contextually-based initiative\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:28.782944Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674520934158336\",\n" +
        "            \"text\": \"Expanded user-facing focusgroup\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:28.097827Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674518040088576\",\n" +
        "            \"text\": \"Innovative context-sensitive help-desk\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:27.404005Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674515133435904\",\n" +
        "            \"text\": \"Synergized bottom-line throughput\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:26.712893Z\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"1411674512189034496\",\n" +
        "            \"text\": \"Reduced client-driven knowledgebase\",\n" +
        "            \"likes\": 0,\n" +
        "            \"liked\": false,\n" +
        "            \"retweets\": 0,\n" +
        "            \"retweeted\": false,\n" +
        "            \"isRetweet\": false,\n" +
        "            \"file\": null,\n" +
        "            \"author\": {\n" +
        "                \"id\": \"1411674502525358080\",\n" +
        "                \"username\": \"syDnPYQwHwWGOc\",\n" +
        "                \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "                \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "                \"banner\": null,\n" +
        "                \"bio\": null,\n" +
        "                \"followers\": 1,\n" +
        "                \"followee\": 0,\n" +
        "                \"following\": true,\n" +
        "                \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "            },\n" +
        "            \"createdAt\": \"2021-07-04T13:13:26.011929Z\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

const val errorTextTooLong: String = "{\n" +
        "    \"errors\": [\n" +
        "        {\n" +
        "            \"field\": \"Text\",\n" +
        "            \"message\": \"the length must be between 1 and 280.\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

const val eitherTextOrFileRequired: String = "{\n" +
        "    \"errors\": [\n" +
        "        {\n" +
        "            \"field\": \"Text\",\n" +
        "            \"message\": \"text is required if no files are provided.\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

const val fileTooBigError: String = "{\n" +
        "    \"errors\": [\n" +
        "        {\n" +
        "            \"field\": \"File\",\n" +
        "            \"message\": \"the size must be below 4MB.\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"