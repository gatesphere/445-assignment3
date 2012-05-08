#!/usr/bin/expect

set user [lrange $argv 0 0]
set password [lrange $argv 1 1]

spawn scp -r src/ $user@moxie.cs.oswego.edu:/opt/445A/
expect "Password:"
send "$password\n"
interact

spawn scp -r src/ $user@altair.cs.oswego.edu:/opt/445A/
expect "Password:"
send "$password\n"
interact

spawn scp -r src/ $user@gee.cs.oswego.edu:/opt/445A/
expect "Password:"
send "$password\n"
interact

