/*
 * DO NOT EDIT.  THIS FILE IS GENERATED FROM e:/builds/moz2_slave/rel-2.0-xr-w32-bld/build/uriloader/exthandler/nsIExternalURLHandlerService.idl
 */

#ifndef __gen_nsIExternalURLHandlerService_h__
#define __gen_nsIExternalURLHandlerService_h__


#ifndef __gen_nsIMIMEInfo_h__
#include "nsIMIMEInfo.h"
#endif

/* For IDL files that don't want to include root IDL files. */
#ifndef NS_NO_VTABLE
#define NS_NO_VTABLE
#endif

/* starting interface:    nsIExternalURLHandlerService */
#define NS_IEXTERNALURLHANDLERSERVICE_IID_STR "56c5c7d3-6fd3-43f8-9429-4397e111453a"

#define NS_IEXTERNALURLHANDLERSERVICE_IID \
  {0x56c5c7d3, 0x6fd3, 0x43f8, \
    { 0x94, 0x29, 0x43, 0x97, 0xe1, 0x11, 0x45, 0x3a }}

/**
 * The external URL handler service is used for finding
 * platform-specific applications for handling particular URLs.
 */
class NS_NO_VTABLE NS_SCRIPTABLE nsIExternalURLHandlerService : public nsISupports {
 public: 

  NS_DECLARE_STATIC_IID_ACCESSOR(NS_IEXTERNALURLHANDLERSERVICE_IID)

  /**
   * Given a URL, looks up the handler info from the OS. This should be
   * overridden by each OS's implementation.
   *
   * @param aURL The URL we are looking for.
   * @param aFound  Was an OS default handler for this URL found?
   * @return  An nsIHanderInfo for the protocol.
   */
  /* nsIHandlerInfo getURLHandlerInfoFromOS (in nsIURI aURL, out boolean aFound); */
  NS_SCRIPTABLE NS_IMETHOD GetURLHandlerInfoFromOS(nsIURI *aURL, PRBool *aFound NS_OUTPARAM, nsIHandlerInfo **_retval NS_OUTPARAM) = 0;

};

  NS_DEFINE_STATIC_IID_ACCESSOR(nsIExternalURLHandlerService, NS_IEXTERNALURLHANDLERSERVICE_IID)

/* Use this macro when declaring classes that implement this interface. */
#define NS_DECL_NSIEXTERNALURLHANDLERSERVICE \
  NS_SCRIPTABLE NS_IMETHOD GetURLHandlerInfoFromOS(nsIURI *aURL, PRBool *aFound NS_OUTPARAM, nsIHandlerInfo **_retval NS_OUTPARAM); 

/* Use this macro to declare functions that forward the behavior of this interface to another object. */
#define NS_FORWARD_NSIEXTERNALURLHANDLERSERVICE(_to) \
  NS_SCRIPTABLE NS_IMETHOD GetURLHandlerInfoFromOS(nsIURI *aURL, PRBool *aFound NS_OUTPARAM, nsIHandlerInfo **_retval NS_OUTPARAM) { return _to GetURLHandlerInfoFromOS(aURL, aFound, _retval); } 

/* Use this macro to declare functions that forward the behavior of this interface to another object in a safe way. */
#define NS_FORWARD_SAFE_NSIEXTERNALURLHANDLERSERVICE(_to) \
  NS_SCRIPTABLE NS_IMETHOD GetURLHandlerInfoFromOS(nsIURI *aURL, PRBool *aFound NS_OUTPARAM, nsIHandlerInfo **_retval NS_OUTPARAM) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetURLHandlerInfoFromOS(aURL, aFound, _retval); } 

#if 0
/* Use the code below as a template for the implementation class for this interface. */

/* Header file */
class nsExternalURLHandlerService : public nsIExternalURLHandlerService
{
public:
  NS_DECL_ISUPPORTS
  NS_DECL_NSIEXTERNALURLHANDLERSERVICE

  nsExternalURLHandlerService();

private:
  ~nsExternalURLHandlerService();

protected:
  /* additional members */
};

/* Implementation file */
NS_IMPL_ISUPPORTS1(nsExternalURLHandlerService, nsIExternalURLHandlerService)

nsExternalURLHandlerService::nsExternalURLHandlerService()
{
  /* member initializers and constructor code */
}

nsExternalURLHandlerService::~nsExternalURLHandlerService()
{
  /* destructor code */
}

/* nsIHandlerInfo getURLHandlerInfoFromOS (in nsIURI aURL, out boolean aFound); */
NS_IMETHODIMP nsExternalURLHandlerService::GetURLHandlerInfoFromOS(nsIURI *aURL, PRBool *aFound NS_OUTPARAM, nsIHandlerInfo **_retval NS_OUTPARAM)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* End of implementation class template. */
#endif


#endif /* __gen_nsIExternalURLHandlerService_h__ */
