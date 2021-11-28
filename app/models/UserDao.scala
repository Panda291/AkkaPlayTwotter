package models

import javax.inject.Inject

@javax.inject.Singleton
class UserDao @Inject()() {

    var users = Seq(
        User("user", "user"),
        User("user1", "user1"),
        User("user2", "user2"),
        User("admin", "admin")
    )
    def lookupUser(u: User): Boolean = {
        //TODO query your database here
        if (users.contains(u)) true else false
    }
}