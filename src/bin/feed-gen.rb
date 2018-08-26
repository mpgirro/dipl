#!/usr/bin/env ruby

require 'optparse'

def item(nr)
    return %Q(
        <item>
            <title>Lorem ipsum</title>
            <enclosure url="http://lorem.ispum.fm/file-#{nr}.mp3"
                       length="#{nr}0000000" type="audio/mpeg"/>
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

def num_digits(n)
    Math.log10(n).to_i + 1
end

def bail(reason)
    puts "No #{reason} argument provided"
    puts USAGE
    exit
end

USAGE = "Usage: feed-gen --feed-count F_COUNT --item-count I_COUNT --feeds-dir F_DIR --properties-dir P_DIR"

feed_count = nil
item_count = nil
feeds_dir = nil
properties_dir = nil

OptionParser.new do |opts|
  opts.banner = USAGE

  opts.on("--feed-count COUNT", "The amound of feeds to generate") do |fc|
    feed_count = fc.to_i
  end

  opts.on("--item-count COUNT", "The amount of items to generate per feed") do |ic|
    item_count = ic.to_i
  end

  opts.on("--feeds-dir DIR", "The directory where to write the feeds to") do |fd|
    feeds_dir = fd
  end

  opts.on("--properties-dir DIR", "The directory where to write the properties JSON fileto") do |pd|
    properties_dir = pd
  end

end.parse! # do the parsing. do it now!

bail("feed-count") if feed_count.nil?
bail("item-count") if item_count.nil?
bail("feeds-dir") if feeds_dir.nil?
bail("properties-dir") if properties_dir.nil?

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
    <description>
      Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed
      diam nonumy eirmod tempor invidunt ut labore et dolore magna
      aliquyam erat, sed diam voluptua
    </description>
    #{items_xml}
  </channel>
</rss>
)

property_lines = []

(1..feed_count).each {|f|

    digit_num = f.to_s.rjust(num_digits(feed_count), "0")
    feed_file = feeds_dir + "/" + "dummy-feed-"+item_count.to_s+"-"+digit_num+".xml"

    File.write(feed_file, feed_xml)

    property_lines << "{\"uri\":\"http://lorem.ipsum.org/#{digit_num}\",\"location\":\"#{feed_file}\",\"numberOfEpisodes\":#{item_count}}"
}

properties_json = "[" + property_lines.inject("") {|s,i| s += ((s=="") ? "" : ",") + i } + "]"
properties_file = properties_dir + "/" + "properties-lorem"+feed_count.to_s+".json"

# puts feed_xml
File.write(properties_file, properties_json)



