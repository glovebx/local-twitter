package xyz.mirage.app.datasource.network

import xyz.mirage.app.business.domain.models.Profile

const val profileListResponse: String = "[\n" +
        "    {\n" +
        "        \"id\": \"1411674329300602880\",\n" +
        "        \"username\": \"yqbww\",\n" +
        "        \"displayName\": \"Loyal Robel I\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/5981376747af230d5a4fce1ad57a603d?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 1,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": true,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674359277293568\",\n" +
        "        \"username\": \"LfCsED\",\n" +
        "        \"displayName\": \"Ms. Tamia Luettgen\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/c6c01c65821808382a55bc3f2470ba55?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674384761884672\",\n" +
        "        \"username\": \"JCXwf\",\n" +
        "        \"displayName\": \"Mr. Florian Schmitt I\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/8c4c11a6fc09eb05456b5d058d4aa2b9?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674414273007616\",\n" +
        "        \"username\": \"WUUOFc\",\n" +
        "        \"displayName\": \"Mr. Brain Lakin\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/52f2d2b9dd24fd6c15eef66f97103680?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674444610408448\",\n" +
        "        \"username\": \"rMYXnFtzVmb\",\n" +
        "        \"displayName\": \"Mr. Ken Cronin\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/6d167e679661d871d883d76922c04942?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 1,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": true,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674474478047232\",\n" +
        "        \"username\": \"uoyjPemDYbTxFSZ\",\n" +
        "        \"displayName\": \"Malvina Orn\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/16f6f3c07c4dac4c6260ebddc6ad26e5?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674502525358080\",\n" +
        "        \"username\": \"syDnPYQwHwWGOc\",\n" +
        "        \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 1,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": true,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674536016875520\",\n" +
        "        \"username\": \"UJbtPtBIP\",\n" +
        "        \"displayName\": \"Frederik Schroeder\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/9bad15f77e7199ec816240abd126c279?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1411674572368908288\",\n" +
        "        \"username\": \"RSNpbKpyEjzuQ\",\n" +
        "        \"displayName\": \"Kailey Cummings\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/9f70375cf5ac010d706832e0ba11ec87?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"id\": \"1413413792913559552\",\n" +
        "        \"username\": \"Test\",\n" +
        "        \"displayName\": \"Testing\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    }\n" +
        "]"

const val followedProfile: String = "    {\n" +
        "        \"id\": \"1411674502525358080\",\n" +
        "        \"username\": \"syDnPYQwHwWGOc\",\n" +
        "        \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 1,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": true,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    }\n"

const val profileResponse: String = "    {\n" +
        "        \"id\": \"1411674502525358080\",\n" +
        "        \"username\": \"syDnPYQwHwWGOc\",\n" +
        "        \"displayName\": \"Ms. Hanna Hyatt\",\n" +
        "        \"image\": \"https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon\",\n" +
        "        \"banner\": null,\n" +
        "        \"bio\": null,\n" +
        "        \"followers\": 0,\n" +
        "        \"followee\": 0,\n" +
        "        \"following\": false,\n" +
        "        \"createdAt\": \"2021-07-04T13:13:23.704286Z\"\n" +
        "    }\n"

val mockProfile = Profile(
    id = "1411674502525358080",
    username = "syDnPYQwHwWGOc",
    displayName = "Ms. Hanna Hyatt",
    image = "https://gravatar.com/avatar/14167ecee94d577b60fc3fd595d501f2?d=identicon",
    bio = null,
    banner = null,
    followee = 0,
    followers = 0,
    following = false,
    createdAt = "2021-07-04T13:13:23.704286Z"
)
