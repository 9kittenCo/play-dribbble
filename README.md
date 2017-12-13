##Play! 2.6
##Scala 2.12.4
##sbt 1.0.4

##Description
You have to create tool to calculate Dribbble stats using dribble public api:
1. For given Dribbble user find all followers
2. For each follower find all shots
3. For each shot find all "likers"
4. Calculate Top10 «likers». People with greater like count descending. 
Implement an api endpoint where user login is a parameter. 

Example:
``` http request
http://0.0.0.0:9000/top10?login=alagoon
```

Output the results as json.