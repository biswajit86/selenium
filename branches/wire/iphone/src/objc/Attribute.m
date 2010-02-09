//
//  Attribute.m
//  iWebDriver
//
//  Copyright 2009 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import "Attribute.h"
#import "WebDriverResource.h"
#import "Element.h"
#import "WebDriverResponse.h"
#import "HTTPVirtualDirectory+AccessViewController.h"
#import "HTTPStaticResource.h"
#import "WebViewController.h"

@implementation Attribute

- (id)initForElement:(Element *)element {
  if (![super init])
    return nil;
  
  // Not retained as per delegate pattern - avoids circular dependancies.
  element_ = element;
  
  return self;
}

+ (Attribute *)attributeDirectoryForElement:(Element *)element {
  return [[[Attribute alloc] initForElement:element] autorelease];
}

- (id<HTTPResource>)elementWithQuery:(NSString *)query {
  NSString *queriedAttribute = [query substringFromIndex:1];
  id<HTTPResource> resource = [contents objectForKey:queriedAttribute];
  if (resource == nil) {
    NSLog(@"First request for %@", queriedAttribute);
    resource = [NamedAttribute
                namedAttributeDirectoryForElement:element_
                andName:queriedAttribute];
    resource = [WebDriverResource resourceWithTarget:resource
                                           GETAction:@selector(getAttribute)
                                          POSTAction:NULL];
    [self setResource:resource withName:queriedAttribute];
  } else {
    NSLog(@"...repeat request for %@", queriedAttribute);
  }
  // Need to delegate back to |super| so |Session| can set the session ID on
  // the response.
  return [super elementWithQuery:query];
}

@end

@implementation NamedAttribute

- (id) initForElement:(Element *)element
              andName:(NSString *)name {
  if (![super init]) {
    return nil;
  }
  // Not retained as per delegate pattern - avoids circular dependancies.
  element_ = element;
  name_ = name;
  return self;
}

+ (NamedAttribute *)namedAttributeDirectoryForElement:(Element *)element
                                              andName:(NSString *)name {
  return [[[NamedAttribute alloc] initForElement:element andName:name]
          autorelease];
}

- (NSString *)getAttribute {
  return [element_ attribute:name_];
}

@end


