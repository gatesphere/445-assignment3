#!/usr/bin/expect

set user [lrange $argv 0 0]
set password [lrange $argv 1 1]

spawn ssh $user@moxie.cs.oswego.edu "cd /opt/445A/; ./build.sh"
expect "Password:"
send "$password\n"
interact

spawn ssh $user@moxie.cs.oswego.edu "cd /opt/445A/; ./build.sh"
expect "Password:"
send "$password\n"
interact

spawn ssh $user@moxie.cs.oswego.edu "cd /opt/445A/; ./build.sh"
expect "Password:"
send "$password\n"
interact
