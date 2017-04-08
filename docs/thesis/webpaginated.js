// ---------------------------------------------
// Helpers
// ---------------------------------------------
function hasClassName(elem,cname) {
  var regex = new RegExp("\\s*\\b" + cname + "\\b","g");
  return regex.test(elem.className);
}

function toggleClassName(elem,cname) {
  var regex = new RegExp("\\s*\\b" + cname + "\\b","g");
  var classes = elem.className;
  if (regex.test(classes)) {
    elem.className = classes.replace( regex, "" );
    return false;
  }
  else {
    elem.className = classes + " " + cname;
    return true;
  }
}

// ---------------------------------------------
// Reliable offset determination
// ---------------------------------------------

function getWindowOffset(elem) {
  var box;
  if (elem.getBoundingClientRect) {
    box = elem.getBoundingClientRect();
  }
  else if (elem.offsetParent && elem.offsetParent.getBoundingClientRect) {
    // text node
    box = elem.offsetParent.getBoundingClientRect();
    box.top = box.top + elem.offsetTop;
    box.left = box.left + elem.offsetLeft;
  }
  else {
    box = { top: 0, left : 0 };
  }
  return box;
}

// ---------------------------------------------
// Expand the toc sections and align headers with the toc.
// ---------------------------------------------

var side = document.getElementsByClassName("sidepanel")[0];
var afterScroll = null;

function alignHeading( elem ) {
  var ofs     = getWindowOffset(elem).top;
  var sideofs = getWindowOffset(side).top;
  if (ofs >= 0 && ofs < sideofs) {
    window.scrollBy(0, ofs - sideofs);
  } 
}

document.addEventListener("scroll", function(ev) {
  if (afterScroll) {
    afterScroll();
    afterScroll = null;
  }
});


[].forEach.call( document.querySelectorAll(".tocitem"), function(item) {
  var target = document.getElementById( item.getAttribute("data-toc-target") );
  if (!target) return;
  var itemContent = item.innerHTML;
  var tocblock = null;
  var toc = item.nextElementSibling;
  if (toc && hasClassName(toc,"tocblock")) { 
    tocblock = toc;
    item.innerHTML = "<span class='unexpanded'></span>" + itemContent;   
  } 
  // on a click
  item.addEventListener( "click", function() {
    // toggle expands class, and set expansion icon
    if (tocblock) {
      if (toggleClassName(tocblock,"expands")) {
        item.innerHTML = "<span class='expanded'></span>" + itemContent;  
      }
      else {
        item.innerHTML = "<span class='unexpanded'></span>" + itemContent;
      }
    }
    // after navigation, align the heading with the toc
    afterScroll = (function() {
      alignHeading(target);
    });    
  });
});
 