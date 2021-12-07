package models

object DebugTweets {
  var tweets = List(
    Tweet("testUser", "@Test", "lorem ipsum", "#MyFirstTestUwU", 8, List[Comment](
      Comment("lorem ipsum", 3),
      Comment("lorem ipsum 2", 5)
    )),
    Tweet("otherUser", "@other", "second tweet", "#test", 8, List[Comment](
      Comment("only one comment for this one", 555555)
    ))
  )
}
