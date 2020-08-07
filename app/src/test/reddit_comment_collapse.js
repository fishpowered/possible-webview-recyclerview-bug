if(!window.fpAttachedRedditCommentCollapse){
	console.log("ATTACHING");
	window.fpAttachedRedditCommentCollapse = true;
	document.body.addEventListener('click', function(e){
	  var showHideChildComments = function(startingComment, startingPadding, hide){
		var currentEl = startingComment;
		startingPadding = parseInt((startingPadding+'').slice(0,-2));
		while(currentEl = currentEl.nextSibling){
		  if(currentEl.nodeType===1){
			if(currentEl.classList.contains("CommentTree__comment") && currentEl.style.paddingLeft){
			  if(parseInt((currentEl.style.paddingLeft+'').slice(0,-2)) > startingPadding){
				// Child comment as visually nested
				// debug: currentEl.style.backgroundColor = '#0f0';
				currentEl.style.display = hide ? 'none' : 'block';
				// if any child nodes had already been collapsed we should reset their styles before collapsing the parent
				currentEl.style.opacity = 1; 
				currentEl.style.maxHeight = 'none';
				currentEl.style.overflow = 'auto';
			  }else{
				// currentEl.style.backgroundColor = '#F00';
				break; // we've hit a sibling/other comment branch
			  }
			}      
		  }      
		}
	  };
	  if(e.target && e.target.nodeName==='P' && Element.prototype.closest){
	  //document.querySelectorAll('')
		var commentRow = e.target.closest('.CommentTree__comment.m-comment');
		if(commentRow){
		  if(commentRow.style.opacity == 0.5){
			commentRow.style.opacity = 1;
			commentRow.style.maxHeight = 'none';
			commentRow.style.overflow = 'auto';
			showHideChildComments(commentRow, commentRow.style.paddingLeft, false); // Nested comments are done with padding, there is no nesting of the html
		  }else{
			commentRow.style.opacity = 0.5;
			commentRow.style.maxHeight = '57px';
			commentRow.style.overflow = 'hidden';
			showHideChildComments(commentRow, commentRow.style.paddingLeft, true);
		  }
		}
	  }
	});
}

