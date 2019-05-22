<?php
	class curl {
		private $ch;
		public $useragent = 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13 IsasDoKapsy';
		
		public function __construct($cookies = null){
			$this->ch = curl_init();
			
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($this->ch, CURLOPT_FOLLOWLOCATION, 1);
			curl_setopt($this->ch, CURLOPT_SSL_VERIFYPEER, false);  
			
			if($cookies){
        		curl_setopt($this->ch, CURLOPT_COOKIESESSION, 1);
				curl_setopt($this->ch, CURLOPT_COOKIEJAR, $cookies);
				curl_setopt($this->ch, CURLOPT_COOKIEFILE, $cookies);
    		}
    		
    		curl_setopt($this->ch, CURLOPT_USERAGENT, $this->useragent);
		}
		
		public function post($url, $data, $cookies = null){
			curl_setopt($this->ch, CURLOPT_URL, $url);
			
			curl_setopt($this->ch, CURLOPT_POST, 1);
			curl_setopt($this->ch, CURLOPT_POSTFIELDS, $data);
			
			$result = curl_exec($this->ch);
			
			return $result;
		}
		
		public function get($url, $cookies = null){
			curl_setopt($this->ch, CURLOPT_URL, $url);
			curl_setopt($this->ch, CURLOPT_POST, 0);
			
			$result = curl_exec($this->ch);
			
			return $result;
		}
		
		public function getCode(){
			return curl_getinfo($this->ch, CURLINFO_HTTP_CODE);
		}
		
	}
?>