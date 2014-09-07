/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class AnEmptyController {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	// The VideoRepository that we are going to store our videos
	// in. We don't explicitly construct a VideoRepository, but
	// instead mark this object as a dependency that needs to be
	// injected by Spring. Our Application class has a method
	// annotated with @Bean that determines what object will end
	// up being injected into this member variable.
	//
	// Also notice that we don't even need a setter for Spring to
	// do the injection.
	//
	@Autowired
	private VideoRepository videos;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH,method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		v.setLikes(0);
		return videos.save(v);
		//Video(String name, String url, long duration, long likes)
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return Lists.newArrayList(videos.findAll());
	}
	/* This is used when a user can only see the video he posted
	public @ResponseBody Collection<Video> getVideoList(
			@RequestBody Video v, 
			Principal p){
		Collection<Video> result = new ArrayList<Video>();
		String username = p.getName();
		videos.findAll();
		for (Video v2: videos.findAll()) {
			//if (v2.)
		}
		return result;
	}
	*/

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}",method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(
			@PathVariable("id") long id,
			HttpServletResponse response){
		Video result = videos.findOne(id);
		if (result == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return result;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like",method=RequestMethod.POST)
	public @ResponseBody Video likeVideo(
			@PathVariable("id") long id,
			Principal p,
			HttpServletResponse response){
		Video result = videos.findOne(id);
		
		// checks if video exists
		if (result == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return result;
		} 

		List<String> likeUsernames = result.getLikeUsernames();
		
		// Checks if the user has already liked the video.
		if (likeUsernames.contains(p.getName())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return result;
		} 
		
		// keep track of users have liked a video
		likeUsernames.add(p.getName());
		result.setLikeUsernames(likeUsernames);
		result.setLikes(likeUsernames.size());
		
		// update Video
		videos.save(result);
		response.setStatus(HttpServletResponse.SC_OK);

		return result;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike",method=RequestMethod.POST)
	public @ResponseBody Video unlikeVideo(
			@PathVariable("id") long id,
			Principal p,
			HttpServletResponse response){
		Video result = videos.findOne(id);
		
		// checks if video exists
		if (result == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return result;
		} 

		List<String> likeUsernames = result.getLikeUsernames();
		
		// Checks if the user has already liked the video.
		if (!likeUsernames.contains(p.getName())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return result;
		} 
		
		// keep track of users have liked a video
		likeUsernames.remove(p.getName());
		result.setLikeUsernames(likeUsernames);
		result.setLikes(likeUsernames.size());
		
		// update Video
		videos.save(result);
		response.setStatus(HttpServletResponse.SC_OK);

		return result;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby",method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(
			@PathVariable("id") long id,
			Principal p,
			HttpServletResponse response){
		Video v = videos.findOne(id);
		Collection<String> result = null;
		
		// checks if video exists
		if (v == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return result;
		} 

		return v.getLikeUsernames();
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			String title){
		return videos.findByName(title);
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			long duration){
		return videos.findByDurationLessThan(duration);
	}
}
