#!/usr/bin/env ruby

def item(nr)
    return %Q(
        <item>
            <title>Lorem ipsum</title>
            <enclosure url="http://lorem.ispum.fm/file-#{nr}.mp3"
                       length="17793193#{nr}" type="audio/mpeg"/>
            <guid isPermaLink="false">http://lorem.ispum.fm/li#{nr}</guid>
            <pubDate>Fri, 13 Jul 2018 16:35:13 +0000</pubDate>
            <description>
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed
                do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                Ut enim ad minim veniam, quis nostrud exercitation ullamco
                laboris nisi ut aliquip ex ea commodo consequat. Duis aute
                irure dolor in reprehenderit in voluptate velit esse cillum
                dolore eu fugiat nulla pariatur. Excepteur sint occaecat
                cupidatat non proident, sunt in culpa qui officia deserunt
                mollit anim id est laborum.
            </description>
        </item>
    )
end

if ARGV[0].nil?
  puts "Error: no item count provided"
  puts USAGE
  exit
end

item_count = ARGV[0].to_i

items_xml = Array
    .new(item_count) {|i| i+1 }
    .map{|i| item(i) }
    .inject("") {|s,i| s += i }

feed_xml = %Q(
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"
     xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
  <channel>
    <title>Lorem ipsum</title>
    <link>http://lorem.ispum.fm</link>
    <language>en-us</language>
    <copyright>cc-by-nc-sa</copyright>
    <description>
      Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed
      diam nonumy eirmod tempor invidunt ut labore et dolore magna
      aliquyam erat, sed diam voluptua
    </description>
    #{items_xml}
  </channel>
</rss>
)

puts feed_xml
